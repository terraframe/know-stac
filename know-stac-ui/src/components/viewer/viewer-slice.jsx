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
        items: [],

        bbox: null,

        // Current extent of the map
        extent: null,

        // Current tab on the search panel
        tab: 0,

        // Current selected item from the map
        selectedItemId: null,

        // Search counter
        count: 0
    },
    reducers: {
        setMapItem: (state, action) => {
            const index = state.items.findIndex(i => i.id === action.payload.id);

            if (index === -1) {
                state.items = [...state.items, action.payload];
            }
            else {
                const items = [...state.items]
                items.splice(index, 1);

                state.items = items;
            }
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
        setExtent: (state, action) => {
            state.extent = action.payload;
        },
        setTab: (state, action) => {
            state.tab = action.payload;
        },
        bbox: (state, action) => {
            state.bbox = action.payload;
        },
        setSelectedItemId: (state, action) => {
            state.selectedItemId = action.payload;
        },
        incrementCount: (state) => {
            state.count += 1;
        },

        default: {}
    },
})

// Action creators are generated for each case reducer function
export const { setMapItem, setActive, setMessages, setCollection, setCriteria, setExtent, bbox, setTab, setSelectedItemId, incrementCount } = viewerSlice.actions

export default viewerSlice.reducer