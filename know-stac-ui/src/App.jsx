import React from "react";
import {
  createHashRouter,
  RouterProvider,
  Outlet
} from "react-router-dom";
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";

import './App.css';

import ErrorPage from "./components/error-page";
import Viewer from "./components/viewer/viewer";


function Root() {

  return (
    <div className="container">
      <Outlet />
    </div>
  );
};

const theme = createTheme({
  palette: {
    primary: {
      light: '#757ce8',
      main: '#394a59',
      dark: '#686c70',
      contrastText: '#fff',
    },
    secondary: {
      light: '#ff7961',
      main: '#777777',
      dark: '#55a36d',
      contrastText: '#fff',
    },
  },
});

const router = createHashRouter([
  {
    path: "/",
    element: <Root />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: "/",
        element: <Viewer />,
      },

    ]
  }
]);

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <RouterProvider router={router} />
      </LocalizationProvider>
    </ThemeProvider>
  );
}
