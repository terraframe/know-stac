import React from 'react';
import PropTypes from 'prop-types';
import { Alert, Box } from "@mui/material";

export default function Alerts({
	messages = []
}) {

	if (messages != null) {

		return (
			< Box >
				{
					messages.map(message => (
						<Alert key={message.key} severity="error">
							{message.message}
						</Alert>
					))
				}
			</Box >
		);
	}
}
Alerts.propTypes = {
	messages: PropTypes.arrayOf(
		PropTypes.shape({
			key: PropTypes.string,
			message: PropTypes.string
		})
	),
}