/* eslint-disable react/no-unstable-nested-components */
/* eslint-disable react/prop-types */
import React, { useState } from 'react';
import { Badge, Box, Button, Grid } from '@mui/material';
import { styled } from '@mui/material/styles';
import { TreeView } from "mui-lazy-tree-view";
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined';
import { useEffectOnce } from 'react-use';
import { useSelector } from 'react-redux';

export default function OrganizationTree(props) {
    const configuration = useSelector((state) => state.configuration.value)

    const { organization, onSelect, onClose } = props;

    const [treeData, setTreeData] = useState([]);
    const [expanded, setExpanded] = useState([]);
    const [selected, setSelected] = useState('');

    const itemComponent = styled('div')({
        treeItemContent: {
            padding: "5px 8px !important",
        },
    });

    const handleNode = ({ object, children }) => {

        const node = { ...object, key: object.code, children: object.size > 0 ? [] : undefined };

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

        fetch(`${configuration.url}/api/organization/get-children?${childrenParams.toString()}`, {
            method: 'GET',
        }).then((response) => {
            if (response.ok) {
                response.json().then((page) => {
                    const rootNodes = page.resultSet.map(object => handleNode({ object }));

                    if (organization != null && organization.code != null) {
                        const params = new URLSearchParams()
                        params.append('code', organization.code);
                        params.append('pageSize', 100);

                        fetch(`${configuration.url}/api/organization/get-ancestor-tree?${params.toString()}`, {
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
    }, [configuration]);

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

        fetch(`${configuration.url}/api/organization/get-children?${params.toString()}`, {
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
            {node.label}
            {node.items > 0 && (
                <Badge badgeContent={node.items} color="primary">
                    <ImageOutlinedIcon />
                </Badge>
            )}
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
            <Grid container spacing={2}>
                <Grid item>
                    <Button color='primary' onClick={() => {
                        onSelect(selected)
                        onClose()
                    }}>Select</Button>
                    <Button color='secondary' onClick={onClose}>Canel</Button>
                </Grid>
            </Grid>

        </Box >);
}