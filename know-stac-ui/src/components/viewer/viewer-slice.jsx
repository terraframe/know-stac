/* eslint-disable no-param-reassign */
import { createSlice } from '@reduxjs/toolkit'

export const viewerSlice = createSlice({
    name: 'viewer',
    initialState: {
        // List of error messages
        messages: [],

        // Flag indicating if the browser is communicating with the server
        active: false,

        // Current collection being displayed
        collection: null,

        // Current criteria used to generate the collection
        criteria: null,

        // STAC item assets being mapped
        item: null,

        bbox: null
    },
    reducers: {
        setMapItem: (state, action) => {
            state.item = action.payload;
        },
        setActive: (state, action) => {
            state.active = action.payload;
        },
        setMessages: (state, action) => {
            state.messages = action.payload;
        },
        setCollection: (state, action) => {

            state.collection = action.payload;
        },
        setCriteria: (state, action) => {
            if (state.criteria == null || state.criteria !== action.payload) {
                state.criteria = action.payload;
            }
        },
        bbox: (state, action) => {
            state.bbox = action.payload;
        },
        default: {}
    },
})

// Action creators are generated for each case reducer function
export const { setMapItem, setActive, setMessages, setCollection, setCriteria, bbox } = viewerSlice.actions

export default viewerSlice.reducer