/* eslint-disable react/prop-types */
import React, { useEffect } from 'react';
import { Autocomplete, Box, Button, debounce, Modal, TextField, Typography } from '@mui/material';
import OrganizationTree from './organization-tree';

const style = {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    minWidth: 600,
    bgcolor: 'background.paper',
    border: '2px solid #000',
    boxShadow: 24,
    p: 4,
};

export default function OrganizationField(props) {

    const { formik, field } = props;

    const [open, setOpen] = React.useState(false);
    const [options, setOptions] = React.useState([]);
    const [inputValue, setInputValue] = React.useState('');
    const [organization, setOrganization] = React.useState({
        code: null,
        label: {
            localizedValue: ''
        }
    });

    const setInputValueDebounce = React.useMemo(
        () =>
            debounce((newInputValue) => {
                setInputValue(newInputValue);
            }, 400),
        [],
    );

    useEffect(() => {
        const params = new URLSearchParams()
        params.append('text', inputValue);

        fetch(`${process.env.REACT_APP_API_URL}/api/organization/search?${params.toString()}`, {
            method: 'GET',
        }).then((response) => {
            if (response.ok) {
                response.json().then((organizations) => {
                    setOptions(organizations);
                });
            }
        });

    }, [inputValue]);

    useEffect(() => {
        // Value changed
        const value = formik.values[field.name];

        if (value != null && value.length > 0 && (organization == null || organization.code !== value)) {
            const params = new URLSearchParams()
            params.append('code', value);

            fetch(`${process.env.REACT_APP_API_URL}/api/organization/get?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then((org) => {
                        setOrganization(org);
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
                value={organization}
                getOptionLabel={(option) => {
                    if (typeof option === 'string') return option;

                    return option.label.localizedValue;
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
                        {option.label.localizedValue}
                    </li>
                }
            />
            <Modal
                open={open}
                onClose={() => setOpen(false)}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
            >
                <Box sx={style}>
                    <Typography id="modal-modal-title" variant="h6" component="h2">
                        Organizations
                    </Typography>
                    <OrganizationTree organization={organization} />
                </Box>
            </Modal>
        </>
    );
}