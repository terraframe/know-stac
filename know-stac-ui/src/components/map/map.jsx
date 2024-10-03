import React, { useRef, useEffect, useState } from 'react';
import maplibregl from 'maplibre-gl';
import { cogProtocol } from '@geomatico/maplibre-cog-protocol';
import 'maplibre-gl/dist/maplibre-gl.css';
import { bboxPolygon, centroid, featureCollection } from '@turf/turf';
import { useDispatch, useSelector } from 'react-redux';
import './map.css';
import { setExtent } from '../viewer/viewer-slice';

export default function Map() {

    const mapItem = useSelector((state) => state.viewer.item)
    const collection = useSelector((state) => state.viewer.collection)
    const mBbox = useSelector((state) => state.viewer.bbox)

    const dispatch = useDispatch()

    const [loaded, setLoaded] = useState(false);

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
                sprite: `${process.env.REACT_APP_SPRITE_URL}`,
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

            // Items source and layer
            map.current.addSource('items', {
                'type': 'geojson',
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
                    "circle-color": "#800000",
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

            setBounds(map.current.getBounds());

            setLoaded(true);
        });

    }, [lng, lat, zoom]);

    // Update the map when the assets change
    useEffect(() => {

        if (mapItem != null) {
            if (map.current.getLayer('map-item')) {
                map.current.removeLayer('map-item');
                map.current.removeSource('map-item');
            };

            map.current.fitBounds(mapItem.item.bbox);

            const params = new URLSearchParams()
            params.append('url', mapItem.item.links[0].href);
            params.append('assets', mapItem.asset);

            const url = `${process.env.REACT_APP_API_URL}/api/tiles/tilejson.json?${params.toString()}`;

            map.current.addLayer({
                'id': 'map-item',
                'type': 'raster',
                'source': {
                    'id': 'map-item',
                    'type': 'raster',
                    'url': url
                },
                'paint': {}
            });

            // const { href } = mapItem.item.assets[mapItem.asset];

            // console.log(`cog://${href}`);

            // map.current.addSource('map-item', {
            //     type: 'raster',
            //     url: `cog://${href}`,
            //     tileSize: 256
            // });

            // map.current.addLayer({
            //     id: 'map-item',
            //     source: 'map-item',
            //     type: 'raster'
            // });
        }
    }, [mapItem])

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

                        const { bbox, title } = link;

                        features.push(centroid(bboxPolygon(bbox), {
                            properties: { label: title }
                        }));
                    })

                    const data = featureCollection(features);

                    map.current.getSource('items').setData(data);
                }

                map.current.fitBounds(collectionBbox);
            }
            else {
                // Update the collection layer
                map.current.getSource('collection').setData(featureCollection([]));
                map.current.getSource('items').setData(featureCollection([]));
            }
        }
    }, [loaded, collection])

    // If the state bbox has changed then update the map
    useEffect(() => {
        if (loaded && mBbox != null) {
            map.current.fitBounds(mBbox);
        }
    }, [loaded, mBbox])

    return (
        <div className="map-wrap">
            <div ref={mapContainer} className="map" />
        </div>
    );
}

