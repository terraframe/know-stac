/* eslint-disable react/prop-types */
import React, { Fragment, useEffect } from 'react';
import { Button, Card, CardActions, CardContent, CardMedia, Collapse, Grid, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import { useDispatch } from 'react-redux';
import { setMapItem, bbox } from '../viewer/viewer-slice';

export default function StacItemCard(props) {
    const { properties, collection, link } = props;
    const [open, setOpen] = React.useState(false);
    const [item, setItem] = React.useState(null);
    const [icon, setIcon] = React.useState(null);

    const dispatch = useDispatch()

    function handleMapIt(asset) {
        dispatch(setMapItem({ item, asset }));
    }


    useEffect(() => {
        if (open && item == null) {
            const params = new URLSearchParams()
            params.append('id', collection.id);
            params.append('href', link.href);

            fetch(`${process.env.REACT_APP_API_URL}/api/query/item?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then(i => {
                        setItem(i);

                        Object.keys(i.assets).forEach(assetName => {
                            const asset = i.assets[assetName];

                            if (asset.href.startsWith("s3:")) {

                                const tParams = new URLSearchParams()
                                tParams.append('url', asset.href);

                                asset.href = `${process.env.REACT_APP_API_URL}/api/aws/download?${tParams.toString()}`;
                            }
                        });

                        if (i.assets['thumbnail-hd'] != null) {
                            setIcon(i.assets['thumbnail-hd']);
                        }
                    });
                }
            });

        }
    }, [open])

    return (
        <Card sx={{ minWidth: 370 }}>
            <CardContent>
                <Typography sx={{ fontSize: 18 }} color="text.primary" gutterBottom>
                    {link.title}
                </Typography>

                <Collapse in={open} timeout="auto" unmountOnExit>
                    {icon != null && (
                        <CardMedia
                            sx={{ height: 140 }}
                            image={icon.href}
                            title={icon.title}
                        />
                    )}

                    <Typography sx={{ fontSize: 14 }} color="text.secondary" gutterBottom>
                        Properties
                    </Typography>
                    {item != null && properties != null && properties.map(field => (
                        <Fragment key={field.name}>
                            {item.properties[field.name] != null && (
                                <Grid container spacing={2}>
                                    <Grid item xs={6}>
                                        <Typography>{field.label}</Typography>
                                    </Grid>
                                    <Grid item xs={6}>
                                        {item.properties[field.name]}
                                    </Grid>
                                </Grid>
                            )}
                        </Fragment>
                    ))}
                    <Typography sx={{ fontSize: 14 }} color="text.secondary" gutterBottom>
                        Assets
                    </Typography>
                    <Table sx={{ minWidth: 350 }} aria-label="simple table">
                        <TableHead>
                            <TableRow>
                                <TableCell>Name</TableCell>
                                <TableCell>Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {item != null && Object.keys(item.assets).filter(asset => item.assets[asset].type === 'image/tiff; application=geotiff; profile=cloud-optimized').map((asset) => (
                                <TableRow
                                    key={asset}
                                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                >
                                    <TableCell component="th" scope="row">
                                        <a href={item.assets[asset].href} target="_blank" rel="noopener noreferrer">
                                            {asset}
                                        </a>
                                    </TableCell>
                                    <TableCell component="th" scope="row">
                                        <Button onClick={() => handleMapIt(asset)}>View on Map</Button>
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
                    Goto Map Extent
                </Button>

            </CardActions>
        </Card >
    );
}
