/* eslint-disable react/prop-types */
import React, { Fragment, useEffect, useMemo } from 'react';
import { useUpdateEffect } from 'react-use';
import { Box, Grid, IconButton, TextField, Typography } from '@mui/material';
import dayjs from 'dayjs';

import { DatePicker } from '@mui/x-date-pickers';
import { Search } from '@mui/icons-material';
import * as yup from 'yup';
import { useFormik } from 'formik';
import { useDispatch, useSelector } from 'react-redux';
import { useSearchParams } from 'react-router-dom';

import { setActive, setCollection, setMessages, setCriteria } from '../viewer/viewer-slice';
import OrganizationField from './organization-field';

export default function SearchForm(props) {
    const { properties } = props;

    const criteria = useSelector((state) => state.viewer.criteria)
    const dispatch = useDispatch()

    const [searchParams, setSearchParams] = useSearchParams();

    const initialValues = useMemo(() => Object.fromEntries(properties.map((field) => {
        const initialValue = field.type !== 'DATE_TIME' ? '' : {
            startDate: null,
            endDate: null
        };

        return [field.name, initialValue];
    })), [properties]);

    const validationSchema = yup.object(Object.fromEntries(properties.map((field) => {
        let valiation = null;

        if (field.type === 'DATE_TIME') {
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
                if (vals[key] == null || vals[key].length === 0) {
                    delete vals[key];
                }
            });

            const parameters = btoa(JSON.stringify({ properties: vals }));

            setSearchParams({ criteria: parameters });
        },
    });

    useEffect(() => {
        if (criteria != null) {
            // Update the form values
            const parameters = JSON.parse(atob(criteria)).properties;

            properties.forEach(field => {
                if (parameters[field.name] != null) {
                    if (field.type === 'DATE_TIME') {
                        formik.setFieldValue(`${field.name}.startDate`, parameters[field.name].startDate != null ? dayjs(parameters[field.name].startDate) : null);
                        formik.setFieldValue(`${field.name}.endDate`, parameters[field.name].endDate != null ? dayjs(parameters[field.name].endDate) : null);
                    }
                    else {
                        formik.setFieldValue(field.name, parameters[field.name]);
                    }
                }
            });
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

            fetch(`${process.env.REACT_APP_API_URL}/api/query/collection?${params.toString()}`, {
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

    }, [criteria]);


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
                {properties.map(field => (
                    <Fragment key={field.name}>
                        {(() => {
                            switch (field.type) {
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
                                    <TextField
                                        margin="dense"
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
                            }
                        })()}
                    </Fragment>
                ))}

                <IconButton type="submit" aria-label="search">
                    <Search style={{ fill: "blue" }} />
                </IconButton>
            </Box>
        </>

    );
}