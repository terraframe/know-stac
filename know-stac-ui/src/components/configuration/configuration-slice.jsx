/* eslint-disable no-param-reassign */
import { createSlice } from '@reduxjs/toolkit'

export const configurationSlice = createSlice({
    name: 'configuration',
    initialState: {
        value: {
            tiling: false,
            url: null,
            loaded: false
        }
    },
    reducers: {
        setConfiguration: (state, action) => {
            state.value = action.payload;
        },
        default: {}
    },
})

// Action creators are generated for each case reducer function
export const { setConfiguration } = configurationSlice.actions

export default configurationSlice.reducer