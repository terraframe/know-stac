/* eslint-disable react/prop-types */
import React, { useEffect, useState } from 'react';
import { Box } from '@mui/material';
import { TreeView } from "mui-lazy-tree-view";

export default function OrganizationTree(props) {

    const { organization } = props;
    const [treeData, setTreeData] = useState([]);
    const [expanded, setExpanded] = useState([]);
    const [selected, setSelected] = useState('');
    const [loaded, setLoaded] = useState(false);

    const handleNode = ({ object, children }) => {

        const node = { ...object, key: object.code, children: [] };

        if (children != null) {
            node.children = children.resultSet.map(n => handleNode(n))
        }

        return node;
    }

    // Load the tree based on the selected organization
    useEffect(() => {
        if (!loaded) {

            if (organization == null || organization.code == null) {
                const params = new URLSearchParams()
                params.append('pageNumber', 1);
                params.append('pageSize', 100);

                fetch(`${process.env.REACT_APP_API_URL}/api/organization/get-children?${params.toString()}`, {
                    method: 'GET',
                }).then((response) => {
                    if (response.ok) {
                        response.json().then((page) => {
                            setTreeData(page.resultSet.map(object => handleNode({ object })));
                        });
                    }
                });
            }
            else {
                const params = new URLSearchParams()
                params.append('code', organization.code);
                params.append('pageSize', 100);

                fetch(`${process.env.REACT_APP_API_URL}/api/organization/get-ancestor-tree?${params.toString()}`, {
                    method: 'GET',
                }).then((response) => {
                    if (response.ok) {
                        response.json().then((node) => {
                            setTreeData([handleNode(node)]);
                        });
                    }
                });
            }

            setLoaded(true);
        }
    }, [organization]);

    const handleToggle = (e, nodeIds) => {
        setExpanded(nodeIds);
    };

    const handleSelect = (e, nodeId) => {
        setSelected(nodeId);
    };

    const onLazyLoad = ({ key, children }) => new Promise((resolve) => {

        if (children && children.length) {
            resolve([]);
        }

        const params = new URLSearchParams()
        params.append('pageNumber', 1);
        params.append('pageSize', 100);
        params.append('code', key);

        fetch(`${process.env.REACT_APP_API_URL}/api/organization/get-children?${params.toString()}`, {
            method: 'GET',
        }).then((response) => {
            if (response.ok) {
                response.json().then((page) => {
                    resolve(page.resultSet.map(object => handleNode({ object })));
                });
            }
        });
    })

    return (
        <Box sx={{ minHeight: 352 }}>
            <TreeView
                expanded={expanded}
                onNodeToggle={handleToggle}
                treeData={treeData}
                selected={selected}
                onNodeSelect={handleSelect}
                titleRender={(node) => (node.label.localizedValue)}
                lazyLoadFn={onLazyLoad}
            />
        </Box>);
}