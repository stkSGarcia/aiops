import * as React from 'react';
import * as Style from '../Style';
import axios from 'axios';
import Config from '../Config';
import { Button, Form, Icon, Input, List, notification, Popconfirm } from 'antd';
import { DocItem, SearchResponse } from './conf/DataStructure';

const FormItem = Form.Item;
const TextArea = Input.TextArea;
const ButtonGroup = Button.Group;

interface SearchState {
  enableEdit: boolean;
  query: string;
  showResult: boolean;
  docs?: DocItem[];
  pageId: number;
  hasPrevPage: boolean;
  hasNextPage: boolean;
  searching: boolean;
}

export default class Search extends React.Component<any, SearchState> {
  pageSize = 10;

  search = (page: number) => {
    this.setState({searching: true});
    const data = {
      query: this.state.query.trim(),
      page: page,
      size: this.pageSize,
    };
    window.scrollTo(0, 490);
    axios.post(`${Config.API_VERSION}/docs/page`, data)
      .then(res => {
        const resultHead = res.data.head;
        if (resultHead.resultCode === Config.RES_SUCCESS) {
          const responseData = res.data.data as SearchResponse;
          // FIXME
          if (responseData.docs.length === 0) {
            this.setState({
              showResult: true,
              docs: page === 1 ? responseData.docs : (this.state.docs || []),
              hasNextPage: false,
              searching: false,
            });
          } else {
            this.setState({
              showResult: true,
              docs: responseData.docs,
              pageId: page,
              hasPrevPage: page > 1,
              hasNextPage: responseData.docs && (responseData.docs.length === this.pageSize),
              searching: false,
            });
          }
        } else {
          notification.error({message: 'ERROR', description: resultHead.message, duration: 3});
          this.setState({searching: false});
        }
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
        this.setState({searching: false});
      });
  }

  renderItem = (item: DocItem, index: number) => {
    if (item === undefined || item === null) {
      return (
        <List.Item key={index} style={{...Style.rowBackground(index), padding: '10px', borderRadius: '10px'}}>
          <div style={{textAlign: 'center'}}>Dirty Data</div>
        </List.Item>
      );
    } else {
      return (
        <List.Item key={index} style={{...Style.rowBackground(index), padding: '10px', borderRadius: '10px'}}>
          <div style={{width: '100%'}}>
            {this.state.enableEdit && (
              <ButtonGroup style={{float: 'right'}}>
                <Button icon="edit" onClick={() => this.edit(item.id)}/>
                <Popconfirm
                  title="确定要删除吗？"
                  icon={<Icon type="warning" style={{color: 'red'}}/>}
                  okText="是"
                  cancelText="否"
                  onConfirm={() => this.delete(item.id)}
                >
                  <Button type="danger" icon="delete"/>
                </Popconfirm>
              </ButtonGroup>
            )}
            <h3 style={{fontWeight: 'bold', display: 'inline'}}>Component: </h3>
            <span dangerouslySetInnerHTML={{__html: item.component || ''}}/>
            <h3 style={{fontWeight: 'bold', marginTop: '10px'}}>Solution:</h3>
            <p style={Style.wordWrap} dangerouslySetInnerHTML={{__html: item.solution || ''}}/>
            <h3 style={{fontWeight: 'bold', marginTop: '30px'}}>Problem:</h3>
            <p style={Style.wordWrap} dangerouslySetInnerHTML={{__html: item.problem || ''}}/>
          </div>
        </List.Item>
      );
    }
  }

  edit = (docId: string) => {
    this.props.history.push({
      pathname: '/mines/update',
      state: {
        docId: docId,
      }
    });
  }

  delete = (docId: string) => {
    axios.delete(`${Config.API_VERSION}/docs`, {params: {id: docId}})
      .then(res => {
        if (res.data.head.resultCode === Config.RES_SUCCESS) {
          notification.success({message: 'SUCCESS', description: '删除成功，请重新查询验证修改结果！', duration: 3});
        } else {
          notification.error({message: 'ERROR', description: res.data.head.message, duration: 3});
        }
      })
      .catch(err => {
        notification.error({message: 'ERROR', description: err.message, duration: 3});
      });
  }

  buttonGroup = () => (
    <ButtonGroup style={{float: 'right'}}>
      <Button
        type="primary"
        disabled={!this.state.hasPrevPage}
        onClick={() => this.search(this.state.pageId - 1)}
      >
        <Icon type="left"/>Back
      </Button>
      <Button
        type="primary"
        disabled={!this.state.hasNextPage}
        onClick={() => this.search(this.state.pageId + 1)}
      >
        Next<Icon type="right"/>
      </Button>
    </ButtonGroup>
  )

  constructor(props: any) {
    super(props);
    const stored = window.sessionStorage.getItem(`refactor/search-${props.location.key}`);
    if (stored) {
      const data = JSON.parse(stored);
      this.state = {
        enableEdit: data.enableEdit,
        query: data.query,
        showResult: data.showResult,
        docs: data.docs,
        pageId: data.pageId,
        hasPrevPage: data.hasPrevPage,
        hasNextPage: data.hasNextPage,
        searching: false,
      };
    } else {
      this.state = {
        enableEdit: props.enableEdit || false,
        query: '',
        showResult: false,
        pageId: 1,
        hasPrevPage: false,
        hasNextPage: false,
        searching: false,
      };
    }
  }

  componentWillReceiveProps(nextProps: any) {
    this.setState({
      enableEdit: nextProps.enableEdit || false,
      query: '',
      showResult: false,
      docs: undefined,
      pageId: 1,
      hasPrevPage: false,
      hasNextPage: false,
      searching: false,
    });
  }

  componentWillUnmount() {
    const data = {
      enableEdit: this.state.enableEdit,
      query: this.state.query,
      showResult: this.state.showResult,
      docs: this.state.docs,
      pageId: this.state.pageId,
      hasPrevPage: this.state.hasPrevPage,
      hasNextPage: this.state.hasNextPage,
    };
    window.sessionStorage.setItem(`refactor/search-${this.props.location.key}`, JSON.stringify(data));
  }

  render() {
    return (
      <div>
        <div style={Style.block}>
          <Form>
            <FormItem>
              <h2 style={{fontWeight: 'bold'}}>请输入问题描述（报错信息，Callstack，Jstack）：</h2>
              <TextArea
                placeholder="Please input search text."
                onChange={(e) => this.setState({query: e.target.value})}
                value={this.state.query}
                style={{height: '300px'}}
              />
            </FormItem>
            <Button
              type="primary"
              icon="search"
              onClick={() => this.search(1)}
              loading={this.state.searching}
            >
              {this.state.searching ? 'Searching' : 'Search'}
            </Button>
          </Form>
        </div>
        {this.state.showResult && (this.state.docs!.length > 0 ? (
          <div style={Style.block}>
            {this.buttonGroup()}
            <h2 style={{fontWeight: 'bold'}}>可能的解决方案：</h2>
            <div style={{clear: 'both'}}/>
            <List dataSource={this.state.docs} renderItem={this.renderItem}/>
            <div style={{marginTop: '10px'}}/>
            {this.buttonGroup()}
            <div style={{clear: 'both'}}/>
          </div>
        ) : (
          <div style={{...Style.block, textAlign: 'center', fontWeight: 'bold', fontSize: '20px'}}>
            暂无数据
          </div>
        ))}
      </div>
    );
  }
}
