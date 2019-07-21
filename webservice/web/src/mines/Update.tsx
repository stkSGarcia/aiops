import * as React from 'react';
import * as Style from '../Style';
import axios from 'axios';
import Config from '../Config';
import { Button, Form, Icon, Input, Modal, notification, Select, Tooltip } from 'antd';
import { DocItem, SearchResponse } from './conf/DataStructure';

const FormItem = Form.Item;
const Option = Select.Option;
const confirm = Modal.confirm;

interface UpdateState {
  enableCreate: boolean;
  id: string;
  doc?: DocItem;
  submitting: boolean;
}

export default Form.create()(class Update extends React.Component<any, UpdateState> {
  handleSubmit = (e: any) => {
    e.preventDefault();
    this.props.form.validateFields((error, values) => {
      if (!error) {
        confirm({
          title: 'Are you sure?',
          okText: 'Yes',
          okType: 'danger',
          cancelText: 'No',
          iconType: 'warning',
          centered: true,
          onOk: () => {
            this.setState({submitting: true});
            if (this.state.enableCreate) {
              const data = {
                userName: values.userName,
                component: values.component.join(','),
                problem: values.problem,
                solution: values.solution,
              };
              axios.put(`${Config.API_VERSION}/docs`, data)
                .then(res => {
                  if (res.data.head.resultCode === Config.RES_SUCCESS) {
                    notification.success({message: 'SUCCESS', description: '录入成功！', duration: 3});
                  } else {
                    notification.error({message: 'ERROR', description: res.data.head.message, duration: 3});
                  }
                  this.setState({submitting: false});
                })
                .catch(err => {
                  notification.error({message: 'ERROR', description: err.message, duration: 3});
                  this.setState({submitting: false});
                });
            } else {
              const data = {
                id: this.state.id,
                userName: values.userName,
                component: values.component.join(','),
                problem: values.problem,
                solution: values.solution,
              };
              axios.patch(`${Config.API_VERSION}/docs`, data)
                .then(res => {
                  if (res.data.head.resultCode === Config.RES_SUCCESS) {
                    notification.success({message: 'SUCCESS', description: '修改成功，请重新查询验证修改结果！', duration: 3});
                    this.props.history.goBack();
                  } else {
                    notification.error({message: 'ERROR', description: res.data.head.message, duration: 3});
                    this.setState({submitting: false});
                  }
                })
                .catch(err => {
                  notification.error({message: 'ERROR', description: err.message, duration: 3});
                  this.setState({submitting: false});
                });
            }
          },
        });
      }
    });
  }

  reset = () => {
    const doc = this.state.doc;
    if (doc !== undefined) {
      this.props.form.setFieldsValue({
        userName: doc.userName,
        component: doc.component.split(','),
        problem: doc.problem,
        solution: doc.solution,
      });
    }
  }

  constructor(props: any) {
    super(props);
    this.state = {
      enableCreate: props.enableCreate || false,
      id: (props.enableCreate || false) ? '' : props.location.state.docId,
      submitting: false,
    };
  }

  componentDidMount() {
    window.scrollTo(0, 0);
    if (!this.state.enableCreate) {
      axios.get(`${Config.API_VERSION}/docs/id`, {params: {id: this.state.id}})
        .then(res => {
          const resultHead = res.data.head;
          const responseData = res.data.data as SearchResponse;
          if (resultHead.resultCode === Config.RES_SUCCESS && responseData.docs.length > 0) {
            this.setState({doc: responseData.docs[0]});
            this.reset();
          } else {
            notification.error({message: 'ERROR', description: resultHead.message, duration: 3});
          }
        })
        .catch(err => {
          notification.error({message: 'ERROR', description: err.message, duration: 3});
        });
    }
  }

  componentWillReceiveProps(nextProps: any) {
    const enableCreate = nextProps.enableCreate || false;
    if (this.state.enableCreate !== enableCreate) {
      this.setState({
        enableCreate: enableCreate,
        id: '',
        doc: undefined,
        submitting: false,
      });
      this.props.form.resetFields();
    } else {
      this.setState({
        enableCreate: enableCreate,
      });
    }
  }

  render() {
    const {getFieldDecorator} = this.props.form;
    const formItemLayout = {
      labelCol: {span: 5},
      wrapperCol: {span: 16},
    };
    const tailFormItemLayout = {
      wrapperCol: {offset: 5},
    };
    return (
      <div style={Style.block}>
        <h2 style={{fontWeight: 'bold'}}>{this.state.enableCreate ? '问题录入' : '问题修改'}</h2>
        <Form onSubmit={this.handleSubmit}>
          <FormItem {...formItemLayout} label="姓名">
            {getFieldDecorator('userName', {
              rules: [{required: true, message: 'Please input your name!', whitespace: true}],
            })(
              <Input/>
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="组件">
            {getFieldDecorator('component', {
              rules: [{required: true, message: 'Please select components!', type: 'array'}],
            })(
              <Select mode="multiple" placeholder="Please select components">
                <Option value="comp_Inceptor">Inceptor</Option>
                <Option value="comp_Hadoop">Hadoop</Option>
                <Option value="comp_Hyperbase">Hyperbase</Option>
                <Option value="comp_Manager">Manager</Option>
                <Option value="comp_Search">Search</Option>
                <Option value="comp_Sophon">Sophon</Option>
                <Option value="comp_TOS">TOS</Option>
                <Option value="comp_OS">OS</Option>
                <Option value="comp_Shiva">Shiva</Option>
              </Select>
            )}
          </FormItem>
          <FormItem
            {...formItemLayout}
            label={(
              <span>
                问题特征细节&nbsp;
                <Tooltip title="报错信息，Callstack，Jstack">
                  <Icon type="question-circle-o"/>
                </Tooltip>
              </span>
            )}
          >
            {getFieldDecorator('problem', {
              rules: [{required: true, message: 'Please input problem details!', whitespace: true}],
            })(
              <Input.TextArea style={{height: '250px'}}/>
            )}
          </FormItem>
          <FormItem
            {...formItemLayout}
            label={(
              <span>
                解决方案&nbsp;
                <Tooltip title="问题的结论性内容，可以是阶段性结论">
                  <Icon type="question-circle-o"/>
                </Tooltip>
              </span>
            )}
          >
            {getFieldDecorator('solution', {
              rules: [{required: true, message: 'Please input problem solutions!', whitespace: true}],
            })(
              <Input.TextArea style={{height: '250px'}}/>
            )}
          </FormItem>
          <FormItem {...tailFormItemLayout}>
            <Button type="primary" htmlType="submit" loading={this.state.submitting}>
              {this.state.submitting ? 'Submitting' : 'Submit'}
            </Button>
            {!this.state.enableCreate &&
            <Button type="danger" style={{marginLeft: '20px'}} onClick={() => this.reset()}>Reset</Button>}
          </FormItem>
        </Form>
      </div>
    );
  }
});
