/* eslint-disable react/prop-types */
import React, { Fragment, useEffect, useMemo } from 'react';
import { useUpdateEffect } from 'react-use';
import { Box, Button, Checkbox, FormControlLabel, Grid, TextField, Typography } from '@mui/material';
import dayjs from 'dayjs';

import { DatePicker } from '@mui/x-date-pickers';
import { Search } from '@mui/icons-material';
import * as yup from 'yup';
import { useFormik } from 'formik';
import { useDispatch, useSelector } from 'react-redux';
import { useSearchParams } from 'react-router-dom';

import { setActive, setCollection, setMessages, setCriteria, setTab, incrementCount } from '../viewer/viewer-slice';
import OrganizationField from './organization-field';
import TextProperty from './text-property';

export default function SearchForm(props) {
    const { properties } = props;

    const configuration = useSelector((state) => state.configuration.value)
    const criteria = useSelector((state) => state.viewer.criteria)
    const extent = useSelector((state) => state.viewer.extent)
    const counter = useSelector((state) => state.viewer.count);

    const dispatch = useDispatch()

    const [searchParams, setSearchParams] = useSearchParams();

    const initialValues = useMemo(() => {
        const map = Object.fromEntries(properties.map((field) => {
            const initialValue = field.type !== 'DATE_TIME' && field.type !== 'DATE' ? '' : {
                startDate: null,
                endDate: null
            };

            return [field.name, initialValue];
        }));

        map.extent = false;

        return map;
    }, [properties]);

    let validationSchema = yup.object(Object.fromEntries(properties.map((field) => {
        let valiation = null;

        if (field.type === 'DATE_TIME' || field.type === 'DATE') {
            valiation = yup.object({
                startDate: yup.date().notRequired(),
                endDate: yup.date().notRequired()
            }).notRequired();
        }
        else {
            valiation = yup.string().notRequired()
        }

        return [field.name, valiation];
    })));

    validationSchema = validationSchema.concat(yup.object({
        extent: yup.boolean().notRequired()
    }));

    // Search parameters have changed, ensure the criteria state is updated
    useEffect(() => {
        dispatch(setCriteria(searchParams.get('criteria')))
    }, [searchParams]);

    const formik = useFormik({
        initialValues,
        validationSchema,
        onSubmit: (values) => {

            const vals = { ...values };

            Object.keys(vals).forEach(key => {

                const property = properties.find(p => p.name === key);

                if (key === 'extent') {
                    delete vals[key];
                }
                else if (vals[key] == null || vals[key].length === 0) {
                    delete vals[key];
                }
                else if (property != null && (property.type === 'DATE' || property.type === 'DATE_TIME')) {
                    if ((vals[key].startDate == null || vals[key].startDate.length === 0)
                        && (vals[key].endDate == null || vals[key].endDate.length === 0)) {
                        delete vals[key];
                    }
                }
            });

            const parameters = { properties: vals, count: counter };

            if (values.extent) {
                parameters.bbox = extent;
            }
            
            setSearchParams({ criteria: btoa(JSON.stringify(parameters)) });

            dispatch(incrementCount())
        },
    });

    useEffect(() => {
        if (criteria != null) {
            // Update the form values
            const map = JSON.parse(atob(criteria))

            const parameters = map.properties;

            if (parameters != null) {
                properties.forEach(field => {
                    if (parameters[field.name] != null) {
                        if (field.type === 'DATE_TIME' || field.type === 'DATE') {
                            formik.setFieldValue(`${field.name}.startDate`, parameters[field.name].startDate != null ? dayjs(parameters[field.name].startDate) : null);
                            formik.setFieldValue(`${field.name}.endDate`, parameters[field.name].endDate != null ? dayjs(parameters[field.name].endDate) : null);
                        }
                        else {
                            formik.setFieldValue(field.name, parameters[field.name]);
                        }
                    }
                });
            }

            formik.setFieldValue("extent", map.bbox != null);
        }
    }, [criteria])

    // If the criteria has changed after the page has been loaded then go get the collection
    useUpdateEffect(() => {
        if (criteria != null) {

            // The criteria has changed 
            dispatch(setMessages(null));

            const params = new URLSearchParams()
            params.append('criteria', criteria);

            dispatch(setActive(true));

            fetch(`${configuration.url}/api/query/collection?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then(collection => {

                        // Add the extent of the items to their link objects
                        for (let i = 0; i < collection.extent.spatial.bbox.length; i += 1) {
                            const bbox = collection.extent.spatial.bbox[i];
                            const link = collection.links[i];

                            link.bbox = bbox;
                        }

                        dispatch(setCollection(collection));
                        dispatch(setTab(1));

                        if (collection.extent.spatial.bbox.length > 999) {
                            dispatch(setMessages([{
                                key: 'size-warning',
                                message: 'The maximum number of items was returned for the collection. There may be additional items which were not displayed.  Please further restrict the query to reduce the number of results',
                                type: 'warning'
                            }]
                            ));
                        }

                    });
                } else {
                    response.json().then(err => {
                        dispatch(setMessages(err.messages));
                    });
                }
            }).finally(() => {
                dispatch(setActive(false));
            });
        }

    }, [configuration, criteria]);


    return (
        <>
            <Grid container spacing={2} className='table-title'>
                <Grid item xs={10}>
                    <Typography variant="h3">
                        Search
                    </Typography>
                </Grid>
            </Grid>

            <Box component="form" onSubmit={formik.handleSubmit} noValidate>
                <FormControlLabel
                    control={<Checkbox checked={formik.values.extent} />}
                    label="Restrict results to extent"
                    name="extent"
                    onChange={formik.handleChange}
                />
                {properties.map(field => (
                    <Fragment key={field.name}>
                        {(() => {
                            switch (field.type) {
                                case 'DATE':
                                case 'DATE_TIME': return (
                                    <Box>
                                        <Typography variant="p">
                                            {field.label}
                                        </Typography>
                                        <Grid container spacing={2}>
                                            <Grid item xs={6}>
                                                <DatePicker
                                                    margin="dense"
                                                    name={`${field.name}.startDate`}
                                                    label="Start"
                                                    value={formik.values[field.name].startDate}
                                                    onChange={(val) => formik.setFieldValue(`${field.name}.startDate`, val)}
                                                    onBlur={formik.handleBlur}
                                                    error={formik.touched[`${field.name}.startDate`] && Boolean(formik.errors[`${field.name}.startDate`])}
                                                    helperText={formik.touched[`${field.name}.startDate`] && formik.errors[`${field.name}.startDate`]}
                                                />
                                            </Grid>
                                            <Grid item xs={6}>

                                                <DatePicker
                                                    margin="dense"
                                                    name={`${field.name}.endDate`}
                                                    label="End"
                                                    value={formik.values[field.name].endDate}
                                                    onChange={(val) => formik.setFieldValue(`${field.name}.endDate`, val)}
                                                    onBlur={formik.handleBlur}
                                                    error={formik.touched[`${field.name}.endDate`] && Boolean(formik.errors[`${field.name}.endDate`])}
                                                    helperText={formik.touched[`${field.name}.endDate`] && formik.errors[`${field.name}.endDate`]}
                                                />
                                            </Grid>
                                        </Grid>
                                    </Box>
                                );
                                case 'NUMBER': return (
                                    <TextField
                                        margin="dense"
                                        type="number"
                                        fullWidth
                                        name={field.name}
                                        label={field.label}
                                        value={formik.values[field.name]}
                                        onChange={formik.handleChange}
                                        onBlur={formik.handleBlur}
                                        error={formik.touched[field.name] && Boolean(formik.errors[field.name])}
                                        helperText={formik.touched[field.name] && formik.errors[field.name]}
                                    />
                                );
                                case 'ORGANIZATION': return (
                                    <OrganizationField field={field} formik={formik} />
                                );
                                case 'LOCATION':
                                    // Location fields are managed inside of the organization field component
                                    return null;
                                default: return (
                                    <TextProperty field={field} formik={formik} />
                                    // <TextField
                                    //     margin="dense"
                                    //     fullWidth
                                    //     name={field.name}
                                    //     label={field.label}
                                    //     value={formik.values[field.name]}
                                    //     onChange={formik.handleChange}
                                    //     onBlur={formik.handleBlur}
                                    //     error={formik.touched[field.name] && Boolean(formik.errors[field.name])}
                                    //     helperText={formik.touched[field.name] && formik.errors[field.name]}
                                    // />
                                );
                            }
                        })()}
                    </Fragment>
                ))}

                <Button type="submit" aria-label="search" startIcon={<Search style={{ fill: "blue" }} />}>Search</Button>
            </Box>
        </>

    );
}