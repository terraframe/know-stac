/* eslint-disable no-param-reassign */
/* eslint-disable react/prop-types */
import React, { Fragment, useEffect } from 'react';
import { Button, Card, CardActions, CardContent, CardMedia, Collapse, Grid, Link, List, ListItem, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import ShareIcon from '@mui/icons-material/Share';
import { useDispatch, useSelector } from 'react-redux';
import { setMapItem, bbox } from '../viewer/viewer-slice';


export default function StacItemCard(props) {
    const { properties, link } = props;
    const items = useSelector((state) => state.viewer.items)

    const configuration = useSelector((state) => state.configuration.value)
    const selectedItemId = useSelector((state) => state.viewer.selectedItemId);

    const [open, setOpen] = React.useState(false);
    const [item, setItem] = React.useState(null);
    const [icon, setIcon] = React.useState(null);

    const dispatch = useDispatch()

    function stringToHash(string) {
        let hash = 0;

        if (string.length === 0) return hash;

        // eslint-disable-next-line no-restricted-syntax
        for (const char of string) {
            // eslint-disable-next-line no-bitwise
            hash ^= char.charCodeAt(0); // Bitwise XOR operation
        }

        return hash;
    }

    function handleMapIt(asset) {
        const { id } = item.assets[asset];

        const i = JSON.parse(JSON.stringify(item));
        i.assets[asset].enabled = !i.assets[asset].enabled;

        setItem(i);

        dispatch(setMapItem({ item, asset, id }));
    }

    function s3ToHttps(obj) {

        if (obj.href.startsWith("s3:")) {

            const uri = obj.href.replace("s3://", "")

            const parts = uri.split('/')

            obj.href = 'https://';
            obj.href += `${parts[0]}.s3.amazonaws.com`;

            for (let j = 1; j < parts.length; j += 1) {
                obj.href += `/${parts[j]}`;
            }
        }

    }

    useEffect(() => {
        if (open && item == null) {
            const url = link.href.startsWith("/") ? (process.env.REACT_APP_API_URL + link.href) : link.href;

            fetch(url, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then(i => {
                        Object.keys(i.assets).forEach(assetName => {
                            const asset = i.assets[assetName];
                            asset.id = stringToHash(`${i.id}-${assetName}`)
                            asset.mappable = false;
                            asset.enabled = (items.findIndex(a => a.id === asset.id) !== -1);

                            if (asset.href.startsWith("http")) {
                                asset.mappable = (asset.type === 'image/tiff; application=geotiff; profile=cloud-optimized');
                            }

                            s3ToHttps(asset);
                        });

                        i.links.forEach(l => {
                            s3ToHttps(l);
                        });

                        setItem(i);

                        if (i.assets.thumbnail != null) {
                            setIcon(i.assets.thumbnail);
                        }
                        else if (i.assets.overview != null) {
                            setIcon(i.assets.overview);
                        }

                    });
                }
            });

        }
    }, [open])

    useEffect(() => {
        if (selectedItemId != null && link.href === selectedItemId) {
            setOpen(true);

            try {
                const element = document.getElementById(selectedItemId);
                if (element) {
                    element.scrollIntoView({ behavior: "smooth", block: "start" });
                }
            } catch (error) {
                // Do nothing
            }
        }
    }, [item, selectedItemId])


    return (
        <Card id={link.href} sx={{ minWidth: 370 }}>
            <CardContent>
                <Grid
                    justify="space-between"
                    container
                    spacing={3}
                >
                    <Grid item>
                        <Typography sx={{ fontSize: 18 }} color="text.primary" gutterBottom>
                            {link.title}
                        </Typography>
                    </Grid>

                    <Grid item>
                        <Link href={link.href} color="primary" underline="hover" target='_blank'>
                            <ShareIcon />
                        </Link>
                    </Grid>
                </Grid>


                <Collapse in={open} timeout="auto" unmountOnExit>
                    {icon != null && (
                        <CardMedia
                            sx={{ height: 140 }}
                            image={icon.href}
                            title={icon.title}
                        />
                    )}

                    <Typography sx={{ fontSize: 16 }} color="text.secondary" gutterBottom>
                        Properties
                    </Typography>
                    <Table sx={{ minWidth: 350 }} aria-label="simple table">
                        <TableBody>

                            {item != null && properties != null && properties.map(field => (
                                <Fragment key={item.id + field.name}>
                                    {item.properties[field.name] != null && (
                                        <TableRow>
                                            <TableCell component="th" scope="row">
                                                {field.label}
                                            </TableCell>
                                            <TableCell component="th" scope="row">
                                                {(() => {
                                                    switch (field.type) {

                                                        case 'ORGANIZATION': return (
                                                            <List>
                                                                {item.properties[field.name].map(organization => (
                                                                    <ListItem key={item.id + organization.code}>
                                                                        {organization.label}
                                                                    </ListItem>
                                                                ))}
                                                            </List>
                                                        );
                                                        case 'LOCATION': return (
                                                            <List>
                                                                {item.properties[field.name].map(location => (
                                                                    <ListItem key={item.id + location.uuid}>
                                                                        {location.label}
                                                                    </ListItem>
                                                                ))}
                                                            </List>
                                                        );
                                                        default: return item.properties[field.name]
                                                    }
                                                })()}
                                            </TableCell>

                                        </TableRow>

                                    )}
                                </Fragment>
                            ))}
                        </TableBody>
                    </Table>

                    <Typography sx={{ fontSize: 16, mt: 4 }} color="text.secondary" gutterBottom>
                        Assets
                    </Typography>
                    <Table sx={{ minWidth: 350 }} aria-label="simple table">
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                                {configuration.tiling && (
                                    <TableCell>Actions</TableCell>
                                )}

                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {item != null && Object.keys(item.assets).map((asset) => (
                                <TableRow
                                    key={item.id + asset}
                                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                >
                                    <TableCell component="th" scope="row">
                                        <a href={item.assets[asset].href} target="_blank" rel="noopener noreferrer">
                                            {asset}
                                        </a>
                                    </TableCell>
                                    <TableCell component="th" scope="row">
                                        {(configuration.tiling && item.assets[asset].mappable) && (
                                            <Button onClick={() => handleMapIt(asset)}>
                                                {(!item.assets[asset].enabled) ? 'View on Map' : 'Remove from Map'}
                                            </Button>
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </Collapse>
            </CardContent>
            <CardActions>
                <Button size="small" onClick={() => setOpen(!open)}>
                    {open ? 'Hide Details' : 'View Details'}
                </Button>
                <Button size="small" onClick={() => dispatch(bbox(link.bbox))}>
                    Goto Item Extent
                </Button>

            </CardActions>
        </Card >
    );
}
