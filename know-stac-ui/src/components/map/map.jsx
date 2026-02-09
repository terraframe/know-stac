import React, { useRef, useEffect, useState } from 'react';
import maplibregl from 'maplibre-gl';
import { v4 as uuidv4 } from 'uuid'
import { cogProtocol } from '@geomatico/maplibre-cog-protocol';
import 'maplibre-gl/dist/maplibre-gl.css';
import { bboxPolygon, centroid, featureCollection } from '@turf/turf';
import { useDispatch, useSelector } from 'react-redux';
import './map.css';
import { setExtent, setSelectedItemId } from '../viewer/viewer-slice';

export default function Map() {

    const items = useSelector((state) => state.viewer.items)
    const collection = useSelector((state) => state.viewer.collection)
    const mBbox = useSelector((state) => state.viewer.bbox)
    const visible = useSelector((state) => state.viewer.visible)

    const dispatch = useDispatch()

    const [loaded, setLoaded] = useState(false);
    const [hoverId, setHoverId] = useState(null);
    const prevHoverId = useRef();

    const mapContainer = useRef(null);
    const map = useRef(null);
    const lng = -99.747653;
    const lat = 39.320699;
    const zoom = 4;

    function isValidBounds(bounds) {

        const ne = bounds.getNorthEast();
        const sw = bounds.getSouthWest();

        if (Math.abs(ne.lng) > 180 || Math.abs(sw.lng) > 180) {
            return false;
        }

        if (Math.abs(ne.lat) > 90 || Math.abs(sw.lat) > 90) {
            return false;
        }

        return true;
    }

    function setBounds(bounds) {
        if (isValidBounds(bounds)) {
            dispatch(setExtent(bounds.toArray().flat()));
        }
    }

    useEffect(() => {
        if (map.current) return; // stops map from intializing more than once

        maplibregl.addProtocol('cog', cogProtocol);

        map.current = new maplibregl.Map({
            container: mapContainer.current,
            style: {
                version: 8,
                name: 'osm',
                metadata: {
                    "mapbox:autocomposite": true
                },
                sources: {
                    osm: {
                        type: "raster",
                        tiles: [
                            "https://osm.gs.mil/tiles/default/{z}/{x}/{y}.png"
                        ],
                        tileSize: 256
                    }
                },
                glyphs: `${process.env.PUBLIC_URL}/glyphs/{fontstack}/{range}.pbf`,
                layers: [
                    {
                        id: 'osm',
                        type: "raster",
                        source: "osm"
                    }
                ]
            },
            center: [lng, lat],
            zoom
        });

        map.current.addControl(new maplibregl.NavigationControl(), 'top-right');

        map.current.on('load', () => {

            // STAC collection source and layer
            map.current.addSource('collection', {
                'type': 'geojson',
                'data': {
                    type: "FeatureCollection",
                    features: []
                }
            });

            map.current.addLayer({
                'id': 'collection',
                'type': 'fill',
                'source': 'collection',
                'paint': {
                    "fill-color": "grey",
                    "fill-opacity": 0.3,
                    "fill-outline-color": "black"
                }
            });

            map.current.addSource('item-extent', {
                'type': 'geojson',
                'data': {
                    type: "FeatureCollection",
                    features: []
                }
            });

            map.current.addLayer({
                'id': 'item-extent',
                'type': 'fill',
                'source': 'item-extent',
                'paint': {
                    "fill-color": "#F8FF00",
                    "fill-opacity": 0.3,
                    "fill-outline-color": "black"
                }
            });

            // Items source and layer
            map.current.addSource('items', {
                'type': 'geojson',
                'promoteId': 'id',
                'data': {
                    type: "FeatureCollection",
                    features: []
                }
            });

            map.current.addLayer({
                'id': 'items',
                'source': 'items',
                "type": "circle",
                "paint": {
                    "circle-radius": 10,
                    "circle-color": [
                        'case',
                        ['boolean', ['feature-state', 'hover'], false],
                        "#3275B8",
                        "#800000"
                    ],
                    "circle-stroke-width": 2,
                    "circle-stroke-color": "#FFFFFF"
                }
            });

            map.current.addLayer({
                "id": 'items-label',
                "type": "symbol",
                "source": "items",
                "paint": {
                    "text-color": "black",
                    "text-halo-color": "#fff",
                    "text-halo-width": 2
                },
                "layout": {
                    "text-field": ["get", "label"],
                    "text-font": ["NotoSansRegular"],
                    "text-offset": [0, 0.6],
                    "text-anchor": "top",
                    "text-size": 12,
                },
            });

            map.current.on("zoomend", () => {
                setBounds(map.current.getBounds());
            });

            map.current.on("moveend", () => {
                setBounds(map.current.getBounds());
            });

            // When the user moves their mouse over the state-fill layer, we'll update the
            // feature state for the feature under the mouse.
            map.current.on('mousemove', 'items', (e) => {
                if (e.features.length > 0) {
                    setHoverId(e.features[0].id);
                }
            });

            map.current.on('mouseleave', 'items', () => {
                setHoverId(null);
            });

            map.current.on('click', 'items', (e) => {

                if (e.features.length > 0) {
                    dispatch(setSelectedItemId(e.features[0].properties.href));
                }
            });

            setBounds(map.current.getBounds());

            setLoaded(true);
        });

    }, [lng, lat, zoom]);

    // Hover effect logic
    useEffect(() => {
        if (map.current != null) {

            if (prevHoverId.current != null) {
                map.current.setFeatureState(
                    { source: 'items', id: prevHoverId.current },
                    { hover: false }
                );
            }

            if (hoverId != null) {
                map.current.setFeatureState(
                    { source: 'items', id: hoverId },
                    { hover: true }
                );

                map.current.getCanvas().style.cursor = 'pointer';
            }
            else {
                map.current.getCanvas().style.cursor = 'default';
            }

            // Update the ref for the next render
            prevHoverId.current = hoverId;
        }
    }, [hoverId]);

    // Update the map when the assets change
    useEffect(() => {

        map.current.getLayersOrder().forEach(layerName => {
            if (layerName.startsWith('map-item')) {
                map.current.removeLayer(layerName);
                map.current.removeSource(layerName);

            }
        })

        if (items != null) {

            items.forEach(item => {
                if (item.item.bbox != null) {
                    map.current.fitBounds(item.item.bbox);
                }

                const params = new URLSearchParams()
                params.append('url', item.item.links[0].href);
                params.append('assets', item.asset);

                const asset = item.item.assets[item.asset];

                if (asset.roles != null && asset.roles.indexOf('multispectral') !== -1) {
                    params.append('multispectral', 'true');
                }
                else if (asset.roles != null && asset.roles.indexOf('elevation') !== -1) {
                    params.append('hillshade', 'true');
                }

                const url = `api/tiles/tilejson.json?${params.toString()}`;

                const id = `map-item-${item.id}`;

                map.current.addLayer({
                    'id': id,
                    'type': 'raster',
                    'source': {
                        'id': id,
                        'type': 'raster',
                        'url': url
                    },
                    'paint': {}
                });

            })
        }
    }, [items])

    // Update the map when the collection changes
    useEffect(() => {

        if (loaded && collection != null) {

            // Zoom to the bounds of the collection
            if (collection.extent.spatial != null && collection.extent.spatial.bbox.length > 0) {

                const collectionBbox = collection.extent.spatial.bbox[0]

                // Update the collection layer
                map.current.getSource('collection').setData(
                    featureCollection([bboxPolygon(collectionBbox)])
                );

                // Update the items layer with new data
                {
                    const features = [];

                    collection.links.filter(link => link.rel === 'item').forEach(link => {

                        const { bbox, title, href } = link;

                        if (bbox != null) {
                            const id = uuidv4()

                            const feature = centroid(bboxPolygon(bbox), {
                                properties: {
                                    label: title, href, id
                                }
                            })
                            feature.id = id;

                            features.push(feature);
                        }
                    })

                    const data = featureCollection(features);

                    map.current.getSource('items').setData(data);

                }

                map.current.fitBounds(collectionBbox);
                map.current.setLayoutProperty('collection', "visibility", visible ? 'visible' : 'none');
            }
            else {
                // Update the collection layer
                map.current.getSource('collection').setData(featureCollection([]));
                map.current.getSource('items').setData(featureCollection([]));
            }
        }
    }, [loaded, collection])

    useEffect(() => {
        if (loaded) {
            map.current.setLayoutProperty('collection', "visibility", visible ? 'visible' : 'none');
        }
    }, [visible])

    // If the state bbox has changed then update the map
    useEffect(() => {
        if (loaded && mBbox != null) {
            // Update the collection layer
            map.current.getSource('item-extent').setData(
                featureCollection([bboxPolygon(mBbox)])
            );

            map.current.fitBounds(mBbox);

        }
    }, [loaded, mBbox])

    return (
        <div className="map-wrap">
            <div ref={mapContainer} className="map" />
        </div>
    );
}

