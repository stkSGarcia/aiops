import * as React from 'react';
import * as Style from '../../Style';
import { BlackWhiteList } from './../conf/DataStructure';
import { Button, Icon, Input, List, Modal, Popconfirm } from 'antd';
// import Config from '../../Config';
// import axios from 'axios';

const ButtonGroup = Button.Group;
// const confirm = Modal.confirm;

interface ListConfProps {
  listRes: BlackWhiteList;
  locationKey: string;
  onChange: any;
}

interface ListConfState {
  blackWhiteList: BlackWhiteList;
  tab: string;
  updateVisible: boolean;
  addVisible: boolean;
  item: string;
  index: number;
  color: string;
  blackPage: number;
  whitePage: number;
  blackData: any[];
  whiteData: any[];
  warning: boolean;
}

export default class ListConf extends React.Component<ListConfProps, ListConfState> {
  delete = (itemIndex: number, color: string) => {
    if (color === 'black') {
      let list = this.state.blackWhiteList.blackList;
      list.splice(itemIndex, 1);
      let newBlackWhiteList = {
        whiteList: this.state.blackWhiteList.whiteList,
        blackList: list,
      };
      this.setState({blackWhiteList: newBlackWhiteList});
    }

    if (color === 'white') {
      let list = this.state.blackWhiteList.whiteList;
      list.splice( itemIndex, 1);
      let newBlackWhiteList = {
        whiteList: list,
        blackList: this.state.blackWhiteList.blackList,
      };
      this.setState({blackWhiteList: newBlackWhiteList});
    }

    this.props.onChange(this.state.blackWhiteList);
  }
  showUpdateModal = (item: string, index: number, color: string) => {
    this.setState({updateVisible: true, item: item, index: index, color: color});
  }
  showAddModal = (color: string) => {
    this.setState({addVisible: true, color: color});
  }
  handleUpdateOk = () => {
    if (this.state.color === 'black') {
      if (this.state.item === '') {
        Modal.error({
          title: '关键字不能为空！',
        });
      } else {
        let list = this.state.blackWhiteList.blackList;
        if (list.indexOf(this.state.item) === -1) {
          list[this.state.index] = this.state.item;
          let newBlackWhiteList = {
            whiteList: this.state.blackWhiteList.whiteList,
            blackList: list,
          };
          let data = this.state.blackData;
          data[this.state.index] = this.state.item;
          this.setState({blackWhiteList: newBlackWhiteList, updateVisible: false, blackData: data});
        } else {
          Modal.error({
            title: '该关键字已存在！',
          });
        }
      }
    }

    if (this.state.color === 'white') {
      if (this.state.item === '') {
        Modal.error({
          title: '关键字不能为空！',
        });
      } else {
        let list = this.state.blackWhiteList.whiteList;
        if (list.indexOf(this.state.item) === -1) {
          list[this.state.index] = this.state.item;
          let newBlackWhiteList = {
            whiteList: list,
            blackList: this.state.blackWhiteList.blackList
          };
          let data = this.state.whiteData;
          data[this.state.index] = this.state.item;
          this.setState({blackWhiteList: newBlackWhiteList, updateVisible: false, whiteData: data});
        } else {
          Modal.error({
            title: '该关键字已存在！',
          });
        }
      }
    }
    this.props.onChange(this.state.blackWhiteList);
  }
  handleUpdateCancel = () => {
    this.setState({updateVisible: false});
  }
  handleAddOk = () => {
    if (this.state.color === 'black') {
      if (this.state.item === '') {
        Modal.error({
          title: '关键字不能为空！',
        });
      } else {
        let list = this.state.blackWhiteList.blackList;
        if (list.indexOf(this.state.item) === -1) {
          list.push(this.state.item);
          let newBlackWhiteList = {
            whiteList: this.state.blackWhiteList.whiteList,
            blackList: list,
          };
          this.setState({blackWhiteList: newBlackWhiteList, addVisible: false});
        } else {
          Modal.error({
            title: '该名单已存在！',
          });
        }
      }
    }

    if (this.state.color === 'white') {
      if (this.state.item === '') {
        Modal.error({
          title: '关键字不能为空！',
        });
      } else {
        let list = this.state.blackWhiteList.whiteList;
        if (list.indexOf(this.state.item) === -1) {
          list.push(this.state.item);
          let newBlackWhiteList = {
            whiteList: list,
            blackList: this.state.blackWhiteList.blackList
          };
          this.setState({blackWhiteList: newBlackWhiteList, addVisible: false});
        } else {
          Modal.error({
            title: '该关键字已存在！',
          });
        }
      }
    }
    this.props.onChange(this.state.blackWhiteList);
  }
  handleAddCancel = () => {
    this.setState({addVisible: false});
  }

  renderItem = (item: string, index: number, color: string) => (
    <List.Item key={index} style={{...Style.rowBackground(index), padding: '4px', borderRadius: '10px'}}>
      <div style={{width: '100%'}}>
        <ButtonGroup style={{float: 'right'}}>
          <Button type="primary" icon="edit" onClick={() => this.showUpdateModal(item, index, color)}/>
          <Popconfirm
            title="确定要删除吗？"
            icon={<Icon type="warning" style={{color: 'red'}}/>}
            okText="是"
            cancelText="否"
            onConfirm={() => this.delete(index, color)}
          >
            <Button type="danger" icon="delete"/>
          </Popconfirm>
        </ButtonGroup>
        <text dangerouslySetInnerHTML={{__html: item}}/>
      </div>
    </List.Item>
  )

  // save = () => {
  //   confirm({
  //     title: 'Do you want to save?',
  //     okText: 'Yes',
  //     okType: 'danger',
  //     cancelText: 'No',
  //     iconType: 'warning',
  //     onOk: () => {
  //       const data = {
  //         blackWhiteList: this.state.blackWhiteList
  //       };
  //       axios.post(`${Config.API_VERSION}/jstack/saving`, data, {timeout: Config.REQUEST_TIMEOUT})
  //         .then(res => {
  //           if (res.data.head.resultCode === Config.RES_SUCCESS) {
  //             notification.success({message: 'SUCCESS', description: '保存成功！', duration: 3});
  //           } else {
  //             notification.error({message: 'ERROR', description: '保存失败!', duration: 3});
  //           }
  //         })
  //         .catch(err => {
  //           notification.error({message: 'ERROR', description: err.message, duration: 3});
  //         });
  //     },
  //   });
  // }

  constructor(props: ListConfProps) {
    super(props);
    const stored = window.sessionStorage.getItem(`jstack/black_white-${props.locationKey}`);
    const storedData = stored ? JSON.parse(stored) : null;
    let blackData: string[] = [];
    let whiteData: string[] = [];

    this.state = {
      blackWhiteList: this.props.listRes,
      tab: stored ? storedData.tab : '1',
      updateVisible: false,
      addVisible: false,
      item: '',
      index: -1,
      color: '',
      blackPage: 1,
      whitePage: 1,
      blackData: blackData,
      whiteData: whiteData,
      warning: false,
    };
  }

  componentDidMount() {
    // const data = {};
    // axios.get(`${Config.API_VERSION}/jstack/black_white`, {params: data})
    //   .then(res => {
    //     const responseData = res.data.data as BlackWhiteResponse;
    //     if (res.data.head.resultCode === Config.RES_SUCCESS) {
    //       // console.log(responseData.blackWhiteList);
    //       this.setState({blackWhiteList: responseData.blackWhiteList});
    //     } else {
    //       notification.error({message: 'ERROR', description: 'Jstack analysis failed!', duration: 3});
    //     }
    //   });
  }

  componentWillUnmount() {
    const data = {
      blackWhiteList: this.state.blackWhiteList,
      tab: this.state.tab,
      updateVisible: this.state.updateVisible,
      addVisible: this.state.addVisible,
      item: this.state.item,
      index: this.state.index,
      color: this.state.color,
      warning: this.state.warning,
    };
    window.sessionStorage.setItem(`jstack/black_white-${this.props.locationKey}`, JSON.stringify(data));
    // this.props.onChange(this.state.blackWhiteList);
  }

  sortList = (s1, s2) => {
    var a = s1.toLowerCase();
    var b = s2.toLowerCase();
    if (a < b) {
      return -1;
    } else if (a > b) {
      return 1;
    } else {
      return 0;
    }
  }

  render() {
    return (
      <div>
        <Modal title="update" visible={this.state.updateVisible} destroyOnClose={true} onOk={this.handleUpdateOk} onCancel={this.handleUpdateCancel}>
          <Input
            onChange={(e) => this.setState({item: e.target.value})}
            value={this.state.item}
            style={{height: '30px'}}
          />
        </Modal>
        <Modal title="add" visible={this.state.addVisible} destroyOnClose={true} onOk={this.handleAddOk} onCancel={this.handleAddCancel}>
          <Input
            onChange={(e) => this.setState({item: e.target.value})}
            style={{height: '30px'}}
          />
        </Modal>

        <h2>White List
          &nbsp;&nbsp;&nbsp;
          <Button
            type="dashed"
            icon="plus"
            style={{color: '#1E90FF'}}
            onClick={() => this.showAddModal('white')}
          />
        </h2>
        <br/>
        <div style={{width: '100%', maxHeight: '250px', overflow: 'scroll'}} >
        <List
          dataSource={this.state.blackWhiteList.whiteList.sort(this.sortList)}
          renderItem={(item, index) => this.renderItem(item, index, 'white')}
        />
        </div>

        <br/>
        <h2>Black List
          &nbsp;&nbsp;&nbsp;
          <Button
            type="dashed"
            icon="plus"
            style={{color: '#1E90FF'}}
            onClick={() => this.showAddModal('black')}
          />
        </h2>
        <br/>
        <div style={{width: '100%', maxHeight: '250px', overflow: 'scroll'}} >
        <List
          dataSource={this.state.blackWhiteList.blackList.sort(this.sortList)}
          renderItem={(item, index) => this.renderItem(item, index, 'black')}
        />
        </div>
      </div>
    );
  }
}
