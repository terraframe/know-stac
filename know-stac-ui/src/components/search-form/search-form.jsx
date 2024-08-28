/* eslint-disable react/prop-types */
import React, { Fragment, useEffect, useMemo } from 'react';
import { useUpdateEffect } from 'react-use';
import { Box, Grid, IconButton, TextField, Typography } from '@mui/material';

import { DateField } from '@mui/x-date-pickers';
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
        const initialValue = field.type !== 'DATE_TIME' ? '' : null;

        return [field.name, initialValue];
    })), [properties]);

    const validationSchema = yup.object(Object.fromEntries(properties.map((field) => {
        let valiation = null;

        if (field.type === 'DATE_TIME') {
            valiation = yup.date().notRequired()
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

            console.log(vals);

            const parameters = btoa(JSON.stringify({ properties: vals }));

            setSearchParams({ criteria: parameters });
        },
    });

    useEffect(() => {
        if (criteria != null) {
            // Update the form values
            const parameters = JSON.parse(atob(criteria)).properties;

            Object.keys(parameters).forEach(name => {
                formik.setFieldValue(name, parameters[name]);
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
                                    <DateField
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