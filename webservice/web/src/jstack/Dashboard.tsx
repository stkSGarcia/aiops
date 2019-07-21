import * as React from 'react';
import TotalFileDashboard from './TotalFileDashboard';
import FileDashboard from './FileDashboard';
import {
  BlackWhiteList,
  BlackWhiteResponse,
  JstackEntryWithLevel,
  JstackFileInfo,
  JstackResponse,
} from './conf/DataStructure';
import { Button, Dropdown, Icon, Menu, Modal, notification, Tabs } from 'antd';
import Config from '../Config';
import axios from 'axios';
import ListConf from './util/ListConf';
import Store from './conf/CacheStore';

const TabPane = Tabs.TabPane;

interface DashboardState {
  totalInfo: JstackFileInfo;
  fileInfo: JstackFileInfo[];
  tab: string;
  visible: boolean;
  visible1: boolean;
  listRes: BlackWhiteList;
  components: string[];
  index: number;
  comp: string;
}

export default class Dashboard extends React.Component<any, DashboardState> {
  isFirst: boolean[] = [];
  threadGroupFirst = true;
  currentIndex: number = 0;
  listResults: BlackWhiteList[] = [];
  threadGroupRes: BlackWhiteList;

  initIsFirst = () => {
    for (var i = 0; i < this.state.components.length; i++) {
      this.isFirst.push(true);
    }
  }

  updateCallstackLevel = (fileInfo: JstackFileInfo[], totalInfo: JstackFileInfo) => {

    let updateLevel = (callStackMap: {
      [callStack: string]: JstackEntryWithLevel,
    }) => {
      Object.keys(callStackMap).forEach((value, index) => {
        var hasFind = false;
        callStackMap[value].keyword = [];

        for (var white of this.state.listRes.whiteList) {
          if (value.includes(white)) {
            callStackMap[value].level = 2;
            callStackMap[value].keyword.push(white);
            hasFind = true;
          }
        }
        if (!hasFind) {
          for (var black of this.state.listRes.blackList) {
            if (value.includes(black)) {
              callStackMap[value].level = 0;
              hasFind = true;
              break;
            }
          }
        }
        if (!hasFind) {
          callStackMap[value].level = 1;
        }
      });
    };

    fileInfo.forEach(jstackFileInfo => {
      updateLevel(jstackFileInfo.callStackMap);
    });
    updateLevel(totalInfo.callStackMap);

    const data = {
      fileInfo: fileInfo,
      totalInfo: totalInfo
    };
    return data;
  }

  updateThreadGroupLevel = (fileInfo: JstackFileInfo[], totalInfo: JstackFileInfo) => {

    let updateLevel = (groupMap: {
      [groupName: string]: JstackEntryWithLevel,
    }) => {
      Object.keys(groupMap).forEach((value, index) => {
        var hasFind = false;
        for (var white of this.state.listRes.whiteList) {
          if (value.includes(white)) {
            groupMap[value].level = 2;
            hasFind = true;
            break;
          }
        }
        if (!hasFind) {
          for (var black of this.state.listRes.blackList) {
            if (value.includes(black)) {
              groupMap[value].level = 0;
              hasFind = true;
              break;
            }
          }
        }
        if (!hasFind) {
          groupMap[value].level = 1;
        }
      });
    };

    fileInfo.forEach(jstackFileInfo => {
      updateLevel(jstackFileInfo.groupMap);
    });
    updateLevel(totalInfo.groupMap);

    const data = {
      fileInfo: fileInfo,
      totalInfo: totalInfo
    };
    return data;
  }

  showModal = (component: string) => {
    let index = this.state.components.indexOf(component);
    this.currentIndex = index;
    if (this.isFirst[index]) {
      axios.get(`${Config.API_VERSION}/jstack/callstack_black_white`, {params: {component: component}})
        .then(res => {
          const responseData = res.data.data as BlackWhiteResponse;
          if (res.data.head.resultCode === Config.RES_SUCCESS) {
            this.setState({listRes: responseData.blackWhiteList, visible: true});
            this.listResults[index] = responseData.blackWhiteList;
          } else {
            notification.error({message: 'ERROR', description: 'Jstack analysis failed!', duration: 3});
          }
        });
    } else {
      this.setState({listRes: this.listResults[index], visible: true});
    }

  }

  handleOk = () => {
    let newResponse = this.updateCallstackLevel(this.state.fileInfo, this.state.totalInfo) as JstackResponse;
    this.setState({fileInfo: newResponse.fileInfo, totalInfo: newResponse.totalInfo, visible: false});
    this.isFirst[this.currentIndex] = false;
  }

  handleReset = () => {
    let component = this.state.components[this.currentIndex];
    axios.get(`${Config.API_VERSION}/jstack/callstack_black_white`, {params: {component: component}})
      .then(res => {
        const responseData = res.data.data as BlackWhiteResponse;
        if (res.data.head.resultCode === Config.RES_SUCCESS) {
          this.setState({listRes: responseData.blackWhiteList});
          this.listResults[this.currentIndex] = responseData.blackWhiteList;
          let newResponse = this.updateCallstackLevel(this.state.fileInfo, this.state.totalInfo) as JstackResponse;
          this.setState({fileInfo: newResponse.fileInfo, totalInfo: newResponse.totalInfo, visible: false});
        } else {
          notification.error({message: 'ERROR', description: 'Jstack analysis failed!', duration: 3});
        }
      });
  }

  handleCancel = () => {
    this.setState({visible: false});
  }

  showModal1 = () => {
    if (this.threadGroupFirst) {
      axios.get(`${Config.API_VERSION}/jstack/threadGroup_black_white`)
        .then(res => {
          const responseData = res.data.data as BlackWhiteResponse;
          if (res.data.head.resultCode === Config.RES_SUCCESS) {
            this.setState({listRes: responseData.blackWhiteList, visible1: true});
            this.threadGroupRes = responseData.blackWhiteList;
          } else {
            notification.error({message: 'ERROR', description: 'Jstack analysis failed!', duration: 3});
          }
        });
    } else {
      this.setState({listRes: this.threadGroupRes, visible1: true});
    }

  }

  handleOk1 = () => {
    let newResponse = this.updateThreadGroupLevel(this.state.fileInfo, this.state.totalInfo) as JstackResponse;
    this.setState({fileInfo: newResponse.fileInfo, totalInfo: newResponse.totalInfo, visible1: false});
    this.threadGroupFirst = false;
  }

  handleReset1 = () => {
    axios.get(`${Config.API_VERSION}/jstack/threadGroup_black_white`)
      .then(res => {
        const responseData = res.data.data as BlackWhiteResponse;
        if (res.data.head.resultCode === Config.RES_SUCCESS) {
          this.setState({listRes: responseData.blackWhiteList, visible1: false});
          this.threadGroupRes = responseData.blackWhiteList;
          let newResponse = this.updateThreadGroupLevel(this.state.fileInfo, this.state.totalInfo) as JstackResponse;
          this.setState({fileInfo: newResponse.fileInfo, totalInfo: newResponse.totalInfo, visible1: false});
        } else {
          notification.error({message: 'ERROR', description: 'Jstack analysis failed!', duration: 3});
        }
      });
  }

  handleCancel1 = () => {
    this.setState({visible1: false});
  }

  save = (blackWhiteList) => {
    if (blackWhiteList) {
      this.setState({listRes: blackWhiteList});
    }
  }

  handleMenuClick = ({key}) => {
    if (key === '1') {
      this.showModal1();
    } else {
      this.showModal(this.state.components[key - 2]);
    }

  }

  constructor(props: any) {
    super(props);
    // const response = props.location.state.response as JstackResponse;
    const stored = window.sessionStorage.getItem(`jstack/dashboard-${props.location.key}`);
    const storedData = stored ? JSON.parse(stored) : null;
    const initList: BlackWhiteList = {
      whiteList: [],
      blackList: [],
    };

    this.state = {
      totalInfo: Store.getState().totalInfo,
      fileInfo: Store.getState().fileInfo,
      tab: stored ? storedData.tab : '1',
      visible: false,
      visible1: false,
      listRes: initList,
      components: Store.getState().components,
      index: 0,
      comp: 'inceptor',
    };
    this.initIsFirst();
  }

  componentWillUnmount() {
    const data = {
      tab: this.state.tab,
    };
    window.sessionStorage.setItem(`jstack/dashboard-${this.props.location.key}`, JSON.stringify(data));
  }

  render() {
    const menu = (
      <Menu onClick={this.handleMenuClick}>
        <Menu.SubMenu title="Stack Trace">
          {this.state.components.map((value, index) => {
              return <Menu.Item key={`${index + 2}`}>{value}</Menu.Item>;
            }
          )}
        </Menu.SubMenu>
        <Menu.Item key="1">Thread Group </Menu.Item>
      </Menu>
    );

    return (
      <div>
        <Modal
          title={'Component: ' + this.state.components[this.currentIndex]}
          visible={this.state.visible}
          destroyOnClose={true}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
          width={'70%'}
          centered={true}
          maskClosable={false}
          footer={[
            <Button key="reset" onClick={this.handleReset}>Reset</Button>,
            <Button key="submit" type="primary" onClick={this.handleOk}>
              Submit
            </Button>,
          ]}
        >
          <ListConf listRes={this.state.listRes} locationKey={this.props.locationKey} onChange={this.save}/>
        </Modal>

        <Modal
          title="Thread Group"
          visible={this.state.visible1}
          destroyOnClose={true}
          onOk={this.handleOk1}
          onCancel={this.handleCancel1}
          width={'70%'}
          centered={true}
          maskClosable={false}
          footer={[
            <Button key="reset" onClick={this.handleReset1}>Reset</Button>,
            <Button key="submit" type="primary" onClick={this.handleOk1}>
              Submit
            </Button>,
          ]}
        >
          <ListConf listRes={this.state.listRes} locationKey={this.props.locationKey} onChange={this.save}/>
        </Modal>

        <h2 style={{marginBottom: '20px', fontWeight: 'bold'}}>Jstack 分析
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <Dropdown overlay={menu}>
            <a style={{color: 'black', fontSize: '16px', fontWeight: 'normal'}}> 配置黑白名单 <Icon type="down"/></a>
          </Dropdown>
        </h2>

        <Tabs activeKey={this.state.tab} onChange={(key) => this.setState({tab: key})}>

          {this.state.fileInfo.length > 1 &&
          <TabPane tab="Summary" key={`${this.state.fileInfo.length + 1}`}>
            <TotalFileDashboard
              fileInfo={this.state.totalInfo}
              history={this.props.history}
              locationKey={this.props.location.key}
            />
          </TabPane>
          }

          {this.state.fileInfo.map((value, index) => (
            <TabPane tab={value.fileName} key={`${index + 1}`}>
              <FileDashboard
                fileInfo={value}
                fileIndex={index}
                history={this.props.history}
                locationKey={this.props.location.key}
              />
            </TabPane>
          ))}
        </Tabs>
      </div>
    );
  }
}
