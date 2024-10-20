/* eslint-disable react/prop-types */
import React, { useEffect } from 'react';
import { Autocomplete, Box, debounce, TextField } from '@mui/material';
import { useSelector } from 'react-redux';
import { useUpdateEffect } from 'react-use';

const style = {
    marginTop: '10px'
};

export default function TextProperty(props) {

    const { formik, field } = props;
    
    const configuration = useSelector((state) => state.configuration.value)
    const criteria = useSelector((state) => state.viewer.criteria)

    const [options, setOptions] = React.useState([]);
    const [inputValue, setInputValue] = React.useState('');
    const [text, setText] = React.useState('');

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

            if (properties != null && properties[field.name] != null) {
                formik.setFieldValue(field.name, properties[field.name]);
            }
        }
    }, [criteria])

    useUpdateEffect(() => {

        if (text == null || inputValue !== text.label) {
            const params = new URLSearchParams()
            params.append('field', field.name);
            params.append('text', inputValue);

            fetch(`${configuration.url}/api/item/values?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then((locations) => {
                        setOptions(locations);
                    });
                }
            });
        }
    }, [configuration, inputValue]);

    useEffect(() => {
        // Value changed
        const fieldValue = formik.values[field.name];

        if (text != null && text.length > 0) {
            const params = new URLSearchParams()
            params.append('field', field.name);
            params.append('text', fieldValue);

            fetch(`${configuration.url}/api/item/values?${params.toString()}`, {
                method: 'GET',
            }).then((response) => {
                if (response.ok) {
                    response.json().then((val) => {
                        setText(val);
                    });
                }
            });
        }

    }, [configuration, formik.values[field.name]]);

    return (
        <Box sx={style}>

            <Autocomplete
                margin="dense"
                fullWidth
                freeSolo
                name={field.name}
                label={field.label}
                options={options}
                value={text}
                getOptionLabel={(option) => {
                    if (typeof option === 'string') return option;

                    return option[0];
                }}
                noOptionsText="No values exist"
                isOptionEqualToValue={(option, value) => option.oid === value.oid}
                onChange={(event, newValue) => {
                    setText(newValue);

                    if (newValue != null) {
                        formik.setFieldValue(field.name, newValue);
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
                    />
                }
                renderOption={(innerProps, option) =>
                    <li {...innerProps} key={option}>
                        {option}
                    </li>
                }
            />
        </Box>
    );
}