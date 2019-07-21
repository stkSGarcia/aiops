// import * as React from 'react';
// import * as Style from '../Style';
// import { BlackWhiteList, BlackWhiteResponse } from './conf/DataStructure';
// import { Button, Icon, Input, List, Modal, notification, Pagination, Popconfirm } from 'antd';
// import Config from '../Config';
// import axios from 'axios';
//
// const ButtonGroup = Button.Group;
// const confirm = Modal.confirm;
//
// interface BlackWhiteState {
//   blackWhiteList: BlackWhiteList;
//   tab: string;
//   updateVisible: boolean;
//   addVisible: boolean;
//   item: string;
//   index: number;
//   color: string;
//   blackPage: number;
//   whitePage: number;
//   blackData: any[];
//   whiteData: any[];
//   warning: boolean;
// }
//
// export default class BlackWhite extends React.Component<any, BlackWhiteState> {
//   pageSize = 5;
//   delete = (itemIndex: number, color: string) => {
//     if (color === 'black') {
//       let list = this.state.blackWhiteList.blackList;
//       list.splice((this.state.blackPage - 1) * this.pageSize + itemIndex, 1);
//       let newBlackWhiteList = {
//         whiteList: this.state.blackWhiteList.whiteList,
//         blackList: list,
//       };
//       this.setState({blackWhiteList: newBlackWhiteList});
//
//       if (this.state.blackWhiteList.blackList.length % this.pageSize === 0) {
//         if (this.state.blackWhiteList.blackList.length === 0) {
//           this.onPageChange(this.state.blackPage, 'black');
//         } else {
//           this.onPageChange(this.state.blackPage - 1, 'black');
//         }
//       } else {
//         this.onPageChange(this.state.blackPage, 'black');
//       }
//     }
//
//     if (color === 'white') {
//       let list = this.state.blackWhiteList.whiteList;
//       list.splice((this.state.whitePage - 1) * this.pageSize + itemIndex, 1);
//       let newBlackWhiteList = {
//         whiteList: list,
//         blackList: this.state.blackWhiteList.blackList,
//       };
//       this.setState({blackWhiteList: newBlackWhiteList});
//       if (this.state.blackWhiteList.whiteList.length % this.pageSize === 0) {
//         if (this.state.blackWhiteList.whiteList.length === 0) {
//           this.onPageChange(this.state.whitePage, 'white');
//         } else {
//           this.onPageChange(this.state.whitePage - 1, 'white');
//         }
//       } else {
//         this.onPageChange(this.state.whitePage, 'white');
//       }
//     }
//   }
//   showUpdateModal = (item: string, index: number, color: string) => {
//     this.setState({updateVisible: true, item: item, index: index, color: color});
//   }
//   showAddModal = (color: string) => {
//     this.setState({addVisible: true, color: color});
//   }
//   handleUpdateOk = () => {
//     if (this.state.color === 'black') {
//       if (this.state.item === '') {
//         Modal.error({
//           title: '关键字不能为空！',
//         });
//       } else {
//         let list = this.state.blackWhiteList.blackList;
//         if (list.indexOf(this.state.item) === -1) {
//           let idx = this.state.index + (this.state.blackPage - 1) * this.pageSize;
//           list[idx] = this.state.item;
//           list.sort(this.sortList);
//           let newBlackWhiteList = {
//             whiteList: this.state.blackWhiteList.whiteList,
//             blackList: list,
//           };
//           let data = this.state.blackData;
//           data[this.state.index] = this.state.item;
//           data.sort(this.sortList);
//           this.setState({blackWhiteList: newBlackWhiteList, updateVisible: false, blackData: data});
//           this.onPageChange(this.state.blackPage, 'black');
//         } else {
//           Modal.error({
//             title: '该关键字已存在！',
//           });
//         }
//       }
//     }
//
//     if (this.state.color === 'white') {
//       if (this.state.item === '') {
//         Modal.error({
//           title: '关键字不能为空！',
//         });
//       } else {
//         let list = this.state.blackWhiteList.whiteList;
//         if (list.indexOf(this.state.item) === -1) {
//           let idx = this.state.index + (this.state.whitePage - 1) * this.pageSize;
//           list[idx] = this.state.item;
//           list.sort(this.sortList);
//           let newBlackWhiteList = {
//             whiteList: list,
//             blackList: this.state.blackWhiteList.blackList
//           };
//           let data = this.state.whiteData;
//           data[this.state.index] = this.state.item;
//           data.sort(this.sortList);
//           this.setState({blackWhiteList: newBlackWhiteList, updateVisible: false, whiteData: data});
//           this.onPageChange(this.state.blackPage, 'black');
//         } else {
//           Modal.error({
//             title: '该关键字已存在！',
//           });
//         }
//       }
//     }
//   }
//
//   handleUpdateCancel = () => {
//     this.setState({updateVisible: false});
//   }
//   handleAddOk = () => {
//     if (this.state.color === 'black') {
//       if (this.state.item === '') {
//         Modal.error({
//           title: '关键字不能为空！',
//         });
//       } else {
//         let list = this.state.blackWhiteList.blackList;
//         if (list.indexOf(this.state.item) === -1) {
//           list.push(this.state.item);
//           list.sort(this.sortList);
//           let newBlackWhiteList = {
//             whiteList: this.state.blackWhiteList.whiteList,
//             blackList: list,
//           };
//           this.setState({blackWhiteList: newBlackWhiteList, addVisible: false});
//           this.onPageChange(this.state.blackPage, 'black');
//         } else {
//           Modal.error({
//             title: '该关键字已存在！',
//           });
//         }
//       }
//     }
//
//     if (this.state.color === 'white') {
//       if (this.state.item === '') {
//         Modal.error({
//           title: '关键字不能为空！',
//         });
//       } else {
//         let list = this.state.blackWhiteList.whiteList;
//         if (list.indexOf(this.state.item) === -1) {
//           list.push(this.state.item);
//           list.sort(this.sortList);
//           let newBlackWhiteList = {
//             whiteList: list,
//             blackList: this.state.blackWhiteList.blackList
//           };
//           this.setState({blackWhiteList: newBlackWhiteList, addVisible: false});
//           this.onPageChange(this.state.whitePage, 'white');
//         } else {
//           Modal.error({
//             title: '该关键字已存在！',
//           });
//         }
//       }
//     }
//   }
//   handleAddCancel = () => {
//     this.setState({addVisible: false});
//   }
//
//   sortList = (s1, s2) => {
//     var a = s1.toLowerCase();
//     var b = s2.toLowerCase();
//     if (a < b) {
//       return -1;
//     } else if (a > b) {
//       return 1;
//     } else {
//       return 0;
//     }
//   }
//   renderItem = (item: string, index: number, color: string) => (
//     <List.Item key={index} style={{...Style.rowBackground(index), padding: '10px', borderRadius: '10px'}}>
//       <div style={{width: '100%'}}>
//         <ButtonGroup style={{float: 'right'}}>
//           <Button type="primary" icon="edit" onClick={() => this.showUpdateModal(item, index, color)}/>
//           <Popconfirm
//             title="确定要删除吗？"
//             icon={<Icon type="warning" style={{color: 'red'}}/>}
//             okText="是"
//             cancelText="否"
//             onConfirm={() => this.delete(index, color)}
//           >
//             <Button type="danger" icon="delete"/>
//           </Popconfirm>
//         </ButtonGroup>
//         <text dangerouslySetInnerHTML={{__html: item}}/>
//       </div>
//     </List.Item>
//   )
//
//   save = () => {
//     confirm({
//       title: 'Do you want to save?',
//       okText: 'Yes',
//       okType: 'danger',
//       cancelText: 'No',
//       iconType: 'warning',
//       onOk: () => {
//         const data = {
//           blackWhiteList: this.state.blackWhiteList
//         };
//         axios.post(`${Config.API_VERSION}/jstack/saving`, data, {timeout: Config.REQUEST_TIMEOUT})
//           .then(res => {
//             if (res.data.head.resultCode === Config.RES_SUCCESS) {
//               notification.success({message: 'SUCCESS', description: '保存成功！', duration: 3});
//             } else {
//               notification.error({message: 'ERROR', description: '保存失败!', duration: 3});
//             }
//           })
//           .catch(err => {
//             notification.error({message: 'ERROR', description: err.message, duration: 3});
//           });
//       },
//     });
//   }
//
//   onPageChange = (page, color: string) => {
//     let end = 0;
//     let data: string[] = [];
//     if (color === 'black') {
//       this.setState({blackPage: page});
//       const begin = this.pageSize * (page - 1);
//       let index = begin;
//       let blackList = this.state.blackWhiteList.blackList;
//       if (page * this.pageSize > blackList.length) {
//         end = blackList.length;
//       } else {
//         end = begin + this.pageSize;
//       }
//       while (index < end) {
//         data.push(blackList[index]);
//         index++;
//       }
//       this.setState({blackData: data});
//     } else {
//       this.setState({whitePage: page});
//       const begin = this.pageSize * (page - 1);
//       let index = begin;
//       let whiteList = this.state.blackWhiteList.whiteList;
//       if (page * this.pageSize > whiteList.length) {
//         end = whiteList.length;
//       } else {
//         end = begin + this.pageSize;
//       }
//       while (index < end) {
//         data.push(whiteList[index]);
//         index++;
//       }
//       this.setState({whiteData: data});
//     }
//   }
//
//   constructor(props: any) {
//     super(props);
//     const response = props.location.state.response as BlackWhiteResponse;
//     const stored = window.sessionStorage.getItem(`jstack/black_white-${props.location.key}`);
//     const storedData = stored ? JSON.parse(stored) : null;
//     let blackList = response.blackWhiteList.blackList;
//     let whiteList = response.blackWhiteList.whiteList;
//     console.log('blackList: ');
//
//     console.log(blackList);
//     let index = 0;
//     let blackData: string[] = [];
//     let whiteData: string[] = [];
//     if (blackList.length < this.pageSize) {
//       blackData = blackList;
//     } else {
//       while (index < this.pageSize) {
//         blackData.push(blackList[index]);
//         index++;
//       }
//     }
//     if (whiteList.length < this.pageSize) {
//       whiteData = whiteList;
//     } else {
//       while (index < this.pageSize) {
//         whiteData.push(whiteList[index]);
//         index++;
//       }
//     }
//     while (index < this.pageSize) {
//       blackData.push(blackList[index]);
//       whiteData.push(whiteList[index]);
//       index++;
//     }
//     this.state = {
//       blackWhiteList: response.blackWhiteList,
//       tab: stored ? storedData.tab : '1',
//       updateVisible: false,
//       addVisible: false,
//       item: '',
//       index: -1,
//       color: '',
//       blackPage: 1,
//       whitePage: 1,
//       blackData: blackData,
//       whiteData: whiteData,
//       warning: false,
//     };
//   }
//
//   componentWillUnmount() {
//     const data = {
//       blackWhiteList: this.state.blackWhiteList,
//       tab: this.state.tab,
//       updateVisible: this.state.updateVisible,
//       addVisible: this.state.addVisible,
//       item: this.state.item,
//       index: this.state.index,
//       color: this.state.color,
//       warning: this.state.warning,
//     };
//     window.sessionStorage.setItem(`jstack/black_white-${this.props.location.key}`, JSON.stringify(data));
//   }
//
//   render() {
//     return (
//       <div>
//         <Modal
//           title="update"
//           visible={this.state.updateVisible}
//           destroyOnClose={true}
//           onOk={this.handleUpdateOk}
//           onCancel={this.handleUpdateCancel}>
//           <Input
//             onChange={(e) => this.setState({item: e.target.value})}
//             value={this.state.item}
//             style={{height: '30px'}}
//           />
//         </Modal>
//         <Modal title="add" visible={this.state.addVisible} destroyOnClose={true} onOk={this.handleAddOk}
//                onCancel={this.handleAddCancel}>
//           <Input
//             onChange={(e) => this.setState({item: e.target.value})}
//             style={{height: '30px'}}
//           />
//         </Modal>
//
//         <h2 style={{marginBottom: '20px', fontWeight: 'bold'}}>配置黑白名单
//           &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
//           <Button
//             style={{marginTop: '20px'}}
//             type="primary"
//             onClick={this.save}
//           >
//             {'Save'}
//           </Button>
//         </h2>
//
//         <h3>
//           White List
//         </h3>
//         <div style={Style.block}>
//           <div style={{clear: 'both'}}/>
//           <List dataSource={this.state.whiteData} renderItem={(item, index) => this.renderItem(item, index, 'white')}/>
//           <Button
//             type="dashed"
//             icon="plus"
//             style={{width: '100%', color: '#1E90FF'}}
//             onClick={() => this.showAddModal('white')}
//           />
//           <Pagination
//             style={{float: 'right', marginTop: '20px'}}
//             total={this.state.blackWhiteList.whiteList.length}
//             current={this.state.whitePage}
//             pageSize={this.pageSize}
//             onChange={(page) => this.onPageChange(page, 'white')}
//             showQuickJumper={true}
//             hideOnSinglePage={true}
//           />
//           <div style={{marginTop: '10px'}}/>
//           <div style={{clear: 'both'}}/>
//         </div>
//
//         <br/>
//         <h3>
//           Black List
//         </h3>
//         <div style={Style.block}>
//           <div style={{clear: 'both'}}/>
//           <List dataSource={this.state.blackData} renderItem={(item, index) => this.renderItem(item, index, 'black')}/>
//           <Button
//             type="dashed"
//             icon="plus"
//             style={{width: '100%', color: '#1E90FF'}}
//             onClick={() => this.showAddModal('black')}
//           />
//           <Pagination
//             style={{float: 'right', marginTop: '20px'}}
//             total={this.state.blackWhiteList.blackList.length}
//             current={this.state.blackPage}
//             pageSize={this.pageSize}
//             onChange={(page) => this.onPageChange(page, 'black')}
//             showQuickJumper={true}
//             hideOnSinglePage={true}
//           />
//           <div style={{marginTop: '10px'}}/>
//           <div style={{clear: 'both'}}/>
//         </div>
//       </div>
//     );
//   }
// }
