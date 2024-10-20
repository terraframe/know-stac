import { configureStore } from '@reduxjs/toolkit'
import viewerReducer from '../components/viewer/viewer-slice'
import configurationReducer from '../components/configuration/configuration-slice'

export default configureStore({
  reducer: {
    configuration: configurationReducer,
    viewer: viewerReducer
  },
})