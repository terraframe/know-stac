# Know-STAC

Know-STAC (Knowledge-Enabled STAC) is an open-source catalog and browser for geospatial data described with the [SpatioTemporal Asset Catalog (STAC)](https://stacspec.org/) specification. It adds a semantic layer to STAC, so items from different programs and organizations can be searched together by place, organization and time, using shared definitions of each.

Know-STAC is being developed for [GeoPlatform.gov](https://www.geoplatform.gov/).

- **Live app:** [knowstac.geoprism.net](https://knowstac.geoprism.net/)
- **API documentation:** [Swagger UI](https://knowstac.geoprism.net/dev/kapi/swagger-ui/index.html)

## Why Know-STAC

STAC is a widely adopted open standard for describing geospatial data. It's usually organized the way data was published, with each provider maintaining its own catalogs and collections. That works within one program, but it makes it hard to answer questions that cut across programs. For example, you might want to know what imagery any agency has collected in a particular watershed over the past year.

Know-STAC addresses this by linking each item to shared reference data. Locations and organizations both come from [Geoprism Registry](https://github.com/terraframe/geoprism-registry), which Know-STAC synchronizes with. Because every item uses the same definitions for place and organization, items from different sources can be searched as one catalog. Collections in Know-STAC are derived from searches rather than defined by publishers. Any query's results can be returned as a STAC Collection, which can be shared and used like any other collection.

## What you can do with Know-STAC

- **Publish STAC Items** through the API, either by posting the item or by giving the URL of an item to ingest. Items can also be removed through the API.
- **Search by place**, using a location hierarchy that shows how many items fall within each location.
- **Search by organization**, using an organization hierarchy with item counts.
- **Filter by area and time**, using a bounding box drawn on the map and a date range.
- **Filter by other item properties**, such as text, numbers, dates and lists of allowed values. These properties are registered in Know-STAC.
- **Create collections from queries.** Any search can be returned as a STAC Collection, with its spatial and temporal extent calculated from the matching items. Each collection has a link that reproduces the query, so it can be shared and opened again later.
- **Preview assets on the map**, including Cloud-Optimized GeoTIFFs. Multispectral, thermal and elevation assets get their own rendering.

## How it works

Know-STAC stores and indexes STAC Items in Elasticsearch. When an item is added, Know-STAC checks that its location and organization values exist in the synchronized hierarchies. It then updates the item counts for those values and everything above them in the hierarchy.

### Where items come from

Know-STAC doesn't create STAC Items itself. Publishers such as IDM generate each item from their own records, tag it with its location and organization from Geoprism Registry, including every level above them, and host the canonical copy. Know-STAC indexes a copy, and the item's `self` link still points to the publisher's version.

Because each item lists every level above its own location and organization, a search on a broad place or organization also finds items tagged at a lower level.

### Derived collections

In most STAC catalogs, a publisher defines collections ahead of time, and each item belongs to one. Know-STAC instead derives collections from searches. The search criteria, such as locations, organizations, dates, area and other properties, are encoded into the collection's ID and its `self` link. Opening that link runs the search again.

A derived collection isn't stored. It's built each time it's requested, from the items that match at that moment. Its spatial and temporal extents are calculated from those items, and it links to each one. This means the collection stays current as publishers add or remove items. It also means a single collection can bring together items from different publishers, programs and agencies.

Location hierarchies are synchronized from a Geoprism Registry instance as labeled property graphs, and organizations are pulled from the same registry. IDM uses the same labeled property graph library to place its sites in a geographic hierarchy.

Know-STAC indexes item metadata only. The data files themselves stay wherever the publisher hosts them. The browser previews raster assets through a [TiTiler](https://developmentseed.org/titiler/) proxy, which reads them straight from their source.

Adding and removing items through the API is limited to clients whose IP address is on an allowlist. Searching is open to everyone.

### STAC support

Know-STAC supports the [STAC Item](https://github.com/radiantearth/stac-spec/blob/master/item-spec/item-spec.md) and [STAC Collection](https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md) specifications. It does not implement the [STAC Catalog](https://github.com/radiantearth/stac-spec/blob/master/catalog-spec/catalog-spec.md) or [STAC API](https://github.com/radiantearth/stac-api-spec) specifications. Its own REST API is documented in the [Swagger UI](https://knowstac.geoprism.net/dev/kapi/swagger-ui/index.html).

### Main technologies

- **Frontend:** React, Redux Toolkit, Material UI and MapLibre GL
- **Backend:** Java and Spring Boot, on the Geoprism and Runway SDK frameworks
- **Search and storage:** Elasticsearch
- **Map previews:** TiTiler
- **API documentation:** OpenAPI (springdoc)

## Repository structure

| Folder | Contents |
| --- | --- |
| `know-stac-server` | Core server: data model, indexing, location and organization services, and REST API controllers |
| `know-stac-api-web` | Web application packaging for the REST API |
| `know-stac-ui` | React browser application |
| `know-stac-ui-web` | Web application that serves the browser, including the TiTiler proxy and app configuration |
| `envcfg` | Environment configuration, with an example properties file |
| `src` | Build and development scripts |

## Related projects

- [Imagery Data Manager (IDM)](https://github.com/terraframe/osmre-uav), which publishes drone imagery products to Know-STAC
- [STAC specification](https://stacspec.org/)

## Contributing

Bug reports and feature requests are welcome as [issues](https://github.com/terraframe/know-stac/issues).

## About

Know-STAC is developed by [TerraFrame](https://terraframe.com) for the U.S. Department of the Interior and GeoPlatform.gov.

## License

Know-STAC is released under the [Apache License, Version 2.0](LICENSE_HEADER).
