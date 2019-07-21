import * as React from 'react';
import * as Style from '../Style';
import Store from './conf/CacheStore';
import vis from 'vis/dist/vis';
import { EntryMap, LockMap } from './conf/DataStructure';

interface LockNetworkState {
  entryMap: EntryMap;
  nodes: vis.DataSet;
  edges: vis.DataSet;
}

export default class LockNetwork extends React.Component<any, LockNetworkState> {
  network: any;

  networkData = (lockMap: LockMap) => {
    const lockThreadNodeOptions = {
      color: {
        background: 'rgba(224, 54, 54, 0.9)',
        highlight: {
          background: 'rgba(227, 78, 78, 0.9)',
        },
        hover: {
          background: 'rgba(227, 78, 78, 0.9)',
        }
      },
      font: {
        color: 'rgba(224, 54, 54, 0.9)',
        size: 10,
        bold: true,
      },
      type: 'entry',
    };
    const waitThreadNodeOptions = {
      color: {
        background: 'rgba(51, 135, 196, 0.9)',
        highlight: {
          background: 'rgba(76, 153, 208, 0.9)',
        },
        hover: {
          background: 'rgba(76, 153, 208, 0.9)',
        }
      },
      type: 'entry',
    };
    const lockNodeOptions = {
      shape: 'hexagon',
      color: {
        background: 'rgba(192, 122, 40, 0.9)',
        highlight: {
          background: 'rgba(214, 140, 56, 0.9)',
        },
        hover: {
          background: 'rgba(214, 140, 56, 0.9)',
        }
      },
      font: {
        color: 'rgba(192, 122, 40, 0.9)',
        size: 6,
        bold: true,
      },
      type: 'lock',
    };
    const nodes = new vis.DataSet();
    const edges = new vis.DataSet();

    Object.keys(lockMap).forEach(lockAddr => {
      const entryList = lockMap[lockAddr];
      const lockList = entryList[0];
      const waitList = entryList[1];
      if (lockList.length !== 0 && waitList.length !== 0) {
        lockList.forEach(lockEntry => {
          let out = 0;
          waitList.forEach(waitEntry => {
            out++;

            const waitThreadNode = {
              id: waitEntry.threadName,
              label: waitEntry.threadName,
              title: waitEntry.threadName,
              value: 1,
              ...waitThreadNodeOptions,
            };
            nodes.get(waitEntry.threadName) === null ? nodes.add(waitThreadNode) : nodes.update(waitThreadNode);

            edges.add({
              from: lockAddr,
              to: waitEntry.threadName,
            });
          });

          const lockNode = {
            id: lockAddr,
            label: '<' + lockAddr + '>',
            title: lockAddr,
            value: out,
            ...lockNodeOptions,
          };
          nodes.get(lockAddr) === null ? nodes.add(lockNode) : nodes.update(lockNode);

          const lockThreadNode = {
            id: lockEntry.threadName,
            label: lockEntry.threadName,
            title: lockEntry.threadName,
            value: out,
            ...lockThreadNodeOptions,
          };
          nodes.get(lockEntry.threadName) === null ? nodes.add(lockThreadNode) : nodes.update(lockThreadNode);

          edges.add({
            from: lockEntry.threadName,
            to: lockAddr,
          });
        });
      }
    });

    return {nodes: nodes, edges: edges};
  }

  onDoubleClick = (props) => {
    const field = 'nodes';
    const id = props[field][0];
    const node = this.state.nodes.get(id);
    switch (node.type) {
      case 'entry':
        this.props.history.push({
          pathname: '/jstack/thread',
          state: {
            entry: this.state.entryMap[node.id],
          }
        });
        break;
      case 'lock':
        this.props.history.push({
          pathname: '/jstack/lock',
          state: {
            lockAddress: node.id,
          }
        });
        break;
      default:
    }
  }

  constructor(props: any) {
    super(props);
    const data = this.networkData(Store.getState().lockMap);
    this.state = {
      entryMap: Store.getState().entryMap,
      nodes: data.nodes,
      edges: data.edges,
    };
  }

  componentDidMount() {
    const container = document.getElementById('network');
    const options = {
      nodes: {
        shape: 'dot',
        color: {
          border: '#ffffff',
          highlight: {
            border: '#ffffff',
          },
          hover: {
            border: '#ffffff',
          }
        },
        font: {
          size: 6,
        },
        borderWidth: 0.6,
        shadow: {
          enabled: true,
          color: '#000000',
          size: 20,
          x: 0,
          y: 0,
        },
        chosen: {
          node: (values, id, selected, hovering) => {
            values.size += 6;
          },
          label: (values, id, selected, hovering) => {
            values.size += 6;
          },
        }
      },
      edges: {
        arrows: {
          to: {
            enabled: true,
            scaleFactor: 0.3,
          }
        },
        smooth: {
          enabled: true,
          type: 'cubicBezier',
          forceDirection: 'none',
          roundness: 0.5,
        },
        color: {
          color: 'rgba(51, 135, 196, 0.9)',
          highlight: 'rgba(35, 107, 157, 0.9)',
          hover: 'rgba(35, 107, 157, 0.9)',
          inherit: false,
          opacity: 0.8,
        },
        font: {
          align: 'bottom',
          size: 5,
        },
      },
      interaction: {
        hover: true,
      },
    };

    if (this.state.nodes.length !== 0) {
      this.network = new vis.Network(container, {nodes: this.state.nodes, edges: this.state.edges}, options);
      this.network.on('doubleClick', this.onDoubleClick);
    }
  }

  render() {
    if (this.state.nodes.length === 0) {
      return <div style={{...Style.block, textAlign: 'center'}}><h3 style={{fontWeight: 'bold'}}>No Lock Network</h3></div>;
    } else {
      return (
        <div style={Style.block}>
          <h3 style={{fontWeight: 'bold', marginBottom: '10px'}}>Lock Network</h3>
          <div style={{width: '100%', paddingTop: '66%', position: 'relative'}}>
            <div
              id="network"
              style={{position: 'absolute', top: 0, left: 0, bottom: 0, right: 0}}
            />
          </div>
        </div>
      );
    }
  }
}
