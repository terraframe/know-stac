import React from 'react';
import { Grid } from '@mui/material';

import './viewer.css';
import Map from '../map/map';
import SearchPanel from '../search-panel/search-panel';

export default function Viewer() {

    return (
        <Grid container spacing={2}>
            <Grid item xs={3}>
                <SearchPanel />
            </Grid>
            <Grid item xs={9}>
                <Map />
            </Grid>
        </Grid>
    );
}

