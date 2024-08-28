/* eslint-disable react/no-unstable-nested-components */
/* eslint-disable react/prop-types */
import React, { useState } from 'react';
import { Badge, Box } from '@mui/material';
import { styled } from '@mui/material/styles';
import { TreeView } from "mui-lazy-tree-view";
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined';
import { useEffectOnce } from 'react-use';

export default function OrganizationTree(props) {

    const { organization, onChange } = props;

    const [treeData, setTreeData] = useState([]);
    const [expanded, setExpanded] = useState([]);
    const [selected, setSelected] = useState('');

    const itemComponent = styled('div')({
        treeItemContent: {
            padding: "5px 8px !important",
        },
    });

    const handleNode = ({ object, children }) => {

        const node = { ...object, key: object.code, children: [] };

        if (children != null) {
            node.children = children.resultSet.map(n => handleNode(n))
        }

        return node;
    }

    const calculateExpanded = (node, keys) => {

        if (node.children != null && node.children.length > 0) {
            keys.push(node.key);

            node.children.forEach(child => calculateExpanded(child, keys));
        }
    }

    // Load the tree based on the selected organization
    useEffectOnce(() => {
        const childrenParams = new URLSearchParams()
        childrenParams.append('pageNumber', 1);
        childrenParams.append('pageSize', 100);

        fetch(`${process.env.REACT_APP_API_URL}/api/organization/get-children?${childrenParams.toString()}`, {
            method: 'GET',
        }).then((response) => {
            if (response.ok) {
                response.json().then((page) => {
                    const rootNodes = page.resultSet.map(object => handleNode({ object }));

                    if (organization != null && organization.code != null) {
                        const params = new URLSearchParams()
                        params.append('code', organization.code);
                        params.append('pageSize', 100);

                        fetch(`${process.env.REACT_APP_API_URL}/api/organization/get-ancestor-tree?${params.toString()}`, {
                            method: 'GET',
                        }).then((resp) => {
                            if (resp.ok) {
                                resp.json().then((node) => {
                                    const rootNode = handleNode(node);

                                    const index = rootNodes.findIndex(n => n.code === rootNode.code);

                                    if (index !== -1) {
                                        rootNodes[index] = rootNode;
                                    }
                                    else {
                                        rootNodes.push(rootNode);
                                    }

                                    setTreeData(rootNodes);

                                    const keys = [];
                                    calculateExpanded(rootNode, keys);

                                    setExpanded(keys);
                                    setSelected(organization.code);
                                });
                            }
                            else {
                                setTreeData(rootNodes);
                            }
                        });
                    }
                    else {
                        setTreeData(rootNodes);
                    }

                });
            }
        });
    }, []);

    const handleToggle = (e, nodeIds) => {
        setExpanded(nodeIds);
    };

    const handleSelect = (e, nodeId) => {
        setSelected(nodeId);

        onChange(nodeId);
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

    const renderTreeItem = (node) => (
        <div style={{ paddingBottom: '10px' }}>
            {node.label.localizedValue}
            <Badge badgeContent={4} color="primary">
                <ImageOutlinedIcon />
            </Badge>
        </div>
    )

    return (
        <Box sx={{ minHeight: 352 }}>
            <TreeView
                expanded={expanded}
                onNodeToggle={handleToggle}
                treeData={treeData}
                selected={selected}
                treeItemClasses={{
                    content: itemComponent.treeItemContent,
                }}
                onNodeSelect={handleSelect}
                titleRender={renderTreeItem}
                lazyLoadFn={onLazyLoad}
            />
        </Box >);
}