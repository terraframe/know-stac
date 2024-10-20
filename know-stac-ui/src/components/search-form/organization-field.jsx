/* eslint-disable react/prop-types */
import React, { useEffect } from 'react';
import { Autocomplete, Box, Button, debounce, Modal, TextField, Typography } from '@mui/material';
import { useUpdateEffect } from 'react-use';
import { useSelector } from 'react-redux';
import OrganizationTree from './organization-tree';
import LocationField from './location-field';

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

const style = {
    marginTop: '10px'
};

export default function OrganizationField(props) {

    const configuration = useSelector((state) => state.configuration.value)
    const { formik, field } = props;

    const [open, setOpen] = React.useState(false);
    const [options, setOptions] = React.useState([]);
    const [fields, setFields] = React.useState([]);
    const [inputValue, setInputValue] = React.useState('');
    const [organization, setOrganization] = React.useState({
        code: null,
        label: ''
    });

    const setInputValueDebounce = React.useMemo(
        () =>
            debounce((newInputValue) => {
                setInputValue(newInputValue);
            }, 400),
        [],
    );

    useUpdateEffect(() => {

        if (organization == null || inputValue !== organization.label) {
            const params = new URLSearchParams()
            params.append('text', inputValue);

            fetch(`${configuration.url}/api/organization/search?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then((organizations) => {
                        setOptions(organizations);
                    });
                }
            });
        }
    }, [configuration, inputValue]);

    useEffect(() => {
        // Value changed
        const value = formik.values[field.name];

        if (value != null && value.length > 0 && (organization == null || organization.code !== value)) {
            const params = new URLSearchParams()
            params.append('code', value);

            fetch(`${configuration.url}/api/organization/get?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then((org) => {
                        setOrganization(org);
                    });
                }
            });
        }

    }, [configuration, formik.values[field.name]]);

    useEffect(() => {
        // Value changed
        if (organization != null && organization.code != null) {
            const params = new URLSearchParams()
            params.append('code', organization.code);

            fetch(`${configuration.url}/api/stac-property/get-for-organization?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then((newFields) => {
                        setFields(newFields);
                    });
                }
            });
        }
        else {
            setFields([]);
        }

    }, [configuration, organization]);


    return (
        <>
            <Box sx={style}>

                <Autocomplete
                    fullWidth
                    freeSolo
                    name={field.name}
                    label={field.label}
                    options={options}
                    value={organization}
                    getOptionLabel={(option) => {
                        if (typeof option === 'string') return option;

                        return option.label;
                    }}
                    noOptionsText="No organizations exists"
                    isOptionEqualToValue={(option, value) => option.oid === value.oid}
                    onChange={(event, newValue) => {
                        setOrganization(newValue);

                        if (newValue != null) {
                            formik.setFieldValue(field.name, newValue.code);
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
                        <li {...innerProps} key={option.code}>
                            {option.label}
                        </li>
                    }
                />
            </Box>
            <Modal
                open={open}
                onClose={() => setOpen(false)}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
            >
                <Box sx={modalStyle}>
                    <Typography id="modal-modal-title" variant="h6" component="h2">
                        {field.label}
                    </Typography>
                    <OrganizationTree
                        organization={organization}
                        onSelect={(code) => {
                            formik.setFieldValue(field.name, code);
                        }}
                        onClose={() => setOpen(false)}
                    />
                </Box>
            </Modal>
            {fields.map((location) =>
                <Box key={`l-${location.name}`} sx={style}>
                    <LocationField field={location} formik={formik} />
                </Box>
            )}

        </>
    );
}