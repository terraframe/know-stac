import { configureStore } from '@reduxjs/toolkit'
import viewerReducer from '../components/viewer/viewer-slice'

export default configureStore({
  reducer: {
    viewer: viewerReducer
  },
})