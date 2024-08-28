/* eslint-disable react/prop-types */
import React, { useEffect } from 'react';
import { Autocomplete, Box, Button, debounce, Modal, TextField, Typography } from '@mui/material';
import { useSelector } from 'react-redux';
import { useUpdateEffect } from 'react-use';
import LocationTree from './location-tree';

const modalStyle = {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    minWidth: 600,
    maxHeight: 700,
    overflow: "scroll",
    bgcolor: 'background.paper',
    border: '2px solid #000',
    boxShadow: 24,
    p: 4,
};

export default function LocationField(props) {

    const { formik, field } = props;

    const criteria = useSelector((state) => state.viewer.criteria)


    const [open, setOpen] = React.useState(false);
    const [options, setOptions] = React.useState([]);
    const [inputValue, setInputValue] = React.useState('');
    const [location, setLocation] = React.useState({
        uuid: null,
        label: ''
    });

    const setInputValueDebounce = React.useMemo(
        () =>
            debounce((newInputValue) => {
                setInputValue(newInputValue);
            }, 400),
        [],
    );

    useEffect(() => {
        if (criteria != null) {
            // Update the form values
            const { properties } = JSON.parse(atob(criteria));

            if (properties[field.name] != null) {
                formik.setFieldValue(field.name, properties[field.name]);
            }
        }
    }, [criteria])

    useUpdateEffect(() => {

        if (location == null || inputValue !== location.label) {
            const params = new URLSearchParams()
            params.append('synchronizationId', field.location.synchronizationId);
            params.append('text', inputValue);

            fetch(`${process.env.REACT_APP_API_URL}/api/location/search?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then((locations) => {
                        setOptions(locations);
                    });
                }
            });
        }
    }, [inputValue]);

    useEffect(() => {
        // Value changed
        const value = formik.values[field.name];

        if (value != null && value.length > 0 && (location == null || location.uuid !== value)) {
            const params = new URLSearchParams()
            params.append('synchronizationId', field.location.synchronizationId);
            params.append('uuid', value);

            fetch(`${process.env.REACT_APP_API_URL}/api/location/get?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then((loc) => {
                        setLocation(loc);
                    });
                }
            });
        }

    }, [formik.values[field.name]]);

    return (
        <>

            <Autocomplete
                fullWidth
                freeSolo
                name={field.name}
                label={field.label}
                options={options}
                value={location}
                getOptionLabel={(option) => {
                    if (typeof option === 'string') return option;

                    return option.label;
                }}
                noOptionsText="No locations exists"
                isOptionEqualToValue={(option, value) => option.oid === value.oid}
                onChange={(event, newValue) => {
                    setLocation(newValue);

                    if (newValue != null) {
                        formik.setFieldValue(field.name, newValue.uuid);
                    }
                    else {
                        formik.setFieldValue(field.name, null);
                    }
                }}
                onInputChange={(event, newInputValue) => {
                    setInputValueDebounce(newInputValue);
                }}
                renderInput={(params) =>
                    <TextField {...params} label={field.label}
                        error={formik.touched[field.name] && Boolean(formik.errors[field.name])}
                        helperText={formik.touched[field.name] && formik.errors[field.name]}
                        InputProps={{
                            ...params.InputProps,
                            endAdornment: (
                                <>
                                    <Button onClick={() => setOpen(true)}>Tree</Button>
                                    {params.InputProps.endAdornment}
                                </>
                            ),
                        }}
                    />
                }
                renderOption={(innerProps, option) =>
                    <li {...innerProps} key={option.uuid}>
                        {option.label}
                    </li>
                }
            />
            <Modal
                open={open}
                onClose={() => setOpen(false)}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
            >
                <Box sx={modalStyle}>
                    <Typography id="modal-modal-title" variant="h6" component="h2">
                        Locations
                    </Typography>
                    <LocationTree location={location} field={field} onChange={(uuid) => {
                        formik.setFieldValue(field.name, uuid);
                    }} />
                </Box>
            </Modal>
        </>
    );
}