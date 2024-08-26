import React, { useState, useEffect } from 'react';
import { Box, Grid, List, ListItem, Paper, Tab, Tabs, Typography } from '@mui/material';
import LoadingOverlay from 'react-loading-overlay-nextgen';
import { useSelector } from 'react-redux';

import Alerts from '../alerts';
import SearchForm from '../search-form/search-form';
import StacItemCard from './stac-item-card';

function CustomTabPanel(props) {
    // eslint-disable-next-line react/prop-types
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
        </div>
    );
}


function a11yProps(index) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

export default function SearchPanel() {

    const active = useSelector((state) => state.viewer.active)
    const messages = useSelector((state) => state.viewer.messages)
    const collection = useSelector((state) => state.viewer.collection)

    const [tab, setTab] = React.useState(0);
    const [properties, setProperties] = useState(null);

    useEffect(() => {
        fetch(`${process.env.REACT_APP_API_URL}/api/stac-property/get-all`, {
            method: 'GET',
        }).then((response) => {
            if (response.ok) {
                response.json().then(props => {
                    setProperties(props);
                });
            }
        });
    }, []);

    return (
        <Paper style={{ maxHeight: '100vh', overflow: 'auto' }}>
            <LoadingOverlay
                active={active}
                spinner
                text='Communicating with server...'
            >
                <Alerts messages={messages} />
                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tabs value={tab} onChange={(e, t) => { setTab(t) }} aria-label="basic tabs example">
                        <Tab label="Search" {...a11yProps(0)} />
                        <Tab label="Results" {...a11yProps(1)} />
                    </Tabs>
                </Box>
                <CustomTabPanel value={tab} index={0}>
                    {properties != null && (
                        <SearchForm properties={properties} />
                    )}
                </CustomTabPanel>
                <CustomTabPanel value={tab} index={1}>
                    {collection != null && (
                        <Box>
                            <Typography variant="h3" component="h3">Collection</Typography>
                            <Grid container spacing={2}>
                                <Grid item xs={2}>
                                    <Typography>Spatial Extent</Typography>
                                </Grid>
                                <Grid item xs={10}>
                                    <Grid container spacing={2}>
                                        <Grid item xs={6}>
                                            {collection.extent.spatial.bbox[0][0]}
                                        </Grid>
                                        <Grid item xs={6}>
                                            {collection.extent.spatial.bbox[0][1]}
                                        </Grid>
                                        <Grid item xs={6}>
                                            {collection.extent.spatial.bbox[1][0]}
                                        </Grid>
                                        <Grid item xs={6}>
                                            {collection.extent.spatial.bbox[1][1]}
                                        </Grid>
                                    </Grid>

                                </Grid>
                                <Grid item xs={2}>
                                    <Typography>Temporal Extent</Typography>
                                </Grid>
                                <Grid item xs={10}>
                                    <Grid container spacing={2}>
                                        <Grid item xs={2}>
                                            <Typography>Start Date</Typography>
                                        </Grid>
                                        <Grid item xs={10}>
                                            {collection.extent.temporal.interval[0]}
                                        </Grid>
                                        <Grid item xs={2}>
                                            <Typography>End Date</Typography>
                                        </Grid>
                                        <Grid item xs={10}>
                                            {collection.extent.temporal.interval[1]}
                                        </Grid>
                                    </Grid>
                                </Grid>
                            </Grid>

                            <Typography variant="h4" component="h4">Items</Typography>
                            <List>
                                {collection.links.filter(link => link.rel === 'item').map((row) => (
                                    <ListItem key={row.href}>
                                        <StacItemCard key={row.href} properties={properties} collection={collection} link={row} />
                                    </ListItem>
                                ))}

                            </List>
                        </Box>

                    )}
                </CustomTabPanel>
            </LoadingOverlay>

        </Paper>


    );
}
