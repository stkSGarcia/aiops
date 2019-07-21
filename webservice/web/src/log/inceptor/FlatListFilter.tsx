import * as React from 'react';
import { Button, Col, Form, Icon, Radio, Select, Switch } from 'antd';
import TimeRangePicker from '../util/TimeRangePicker';
import NumberRangePicker from '../util/NumberRangePicker';

const FormItem = Form.Item;
const Option = Select.Option;
const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;

export const FlatListFilter = Form.create({
  onFieldsChange(props: any, changedFields: any) {
    props.onChange(changedFields);
  },
  mapPropsToFields(props: any) {
    return {
      sorter: Form.createFormField({value: props.sorter.value, ...props.sorter}),
      order: Form.createFormField({value: props.order.value, ...props.order}),
      smart: Form.createFormField({value: props.smart.value, ...props.smart}),
      goal: Form.createFormField({value: props.goal.value, ...props.goal}),
      task: Form.createFormField({value: props.task.value, ...props.task}),
      time: Form.createFormField({value: props.time.value, ...props.time}),
      duration: Form.createFormField({value: props.duration.value, ...props.duration}),
    };
  },
})((props: any) => {
  const {getFieldDecorator} = props.form;
  const formItemLayout = {
    labelCol: {span: 4},
    wrapperCol: {span: 20},
  };
  const onSmartChange = (value) => {
    if (value) {
      props.form.setFieldsValue({goal: [], task: []});
    }
  };
  const checkTimeRange = (rule, value, callback) => {
    if (value.from > value.to) {
      callback('End time must greater than begin time!');
    } else {
      callback();
    }
  };
  const checkDuration = (rule, value, callback) => {
    if (value.min !== undefined && value.max !== undefined && value.min > value.max) {
      callback('Max duration must greater than min duration!');
    } else {
      callback();
    }
  };
  const handleSubmit = (e) => {
    e.preventDefault();
    props.form.validateFields((err) => {
      if (!err) {
        props.onSubmit();
      }
    });
  };
  return (
    <Form onSubmit={handleSubmit} onReset={props.onReset} hideRequiredMark={true}>
      <FormItem {...formItemLayout} label="Sort By">
        <Col span={14}>
          <FormItem>
            {getFieldDecorator('sorter', {
              rules: [{required: true}]
            })(
              <Select placeholder="Please select">
                <Option value="0">Start Time</Option>
                <Option value="1">End Time</Option>
                <Option value="2">Duration</Option>
              </Select>
            )}
          </FormItem>
        </Col>
        <Col span={4} offset={1}>
          <FormItem>
            {getFieldDecorator('order', {
              rules: [{required: true}]
            })(
              <RadioGroup buttonStyle="solid">
                <RadioButton value="asc"><Icon type="arrow-up"/></RadioButton>
                <RadioButton value="desc"><Icon type="arrow-down"/></RadioButton>
              </RadioGroup>
            )}
          </FormItem>
        </Col>
      </FormItem>

      <FormItem {...formItemLayout} label="Filter">
        <Col span={2}>
          <FormItem>
            {getFieldDecorator('smart', {valuePropName: 'checked'})(
              <Switch checkedChildren="Smart" unCheckedChildren="Normal" onChange={onSmartChange}/>
            )}
          </FormItem>
        </Col>
        <Col span={7} offset={1}>
          <FormItem>
            {getFieldDecorator('goal')(
              <Select mode="multiple" placeholder="Goal" disabled={props.smart.value}>
                <Option value="gce">Compile Error</Option>
                <Option value="gs">Complete Success</Option>
                <Option value="ge">Complete Error</Option>
                <Option value="gi">Incomplete</Option>
              </Select>
            )}
          </FormItem>
        </Col>
        <Col span={7} offset={1}>
          <FormItem>
            {getFieldDecorator('task')(
              <Select mode="multiple" placeholder="Task" disabled={props.smart.value}>
                <Option value="tc">Complete without error</Option>
                <Option value="tce">Complete with error</Option>
                <Option value="ti">Incomplete without error</Option>
                <Option value="tie">Incomplete with error</Option>
              </Select>
            )}
          </FormItem>
        </Col>
      </FormItem>

      <FormItem {...formItemLayout} label="Time Range">
        {getFieldDecorator('time', {
          rules: [{validator: checkTimeRange}],
        })(<TimeRangePicker/>)}
      </FormItem>

      <FormItem {...formItemLayout} label="Duration Filter" style={{marginBottom: 0}}>
        {getFieldDecorator('duration', {
          rules: [{validator: checkDuration}],
        })(<NumberRangePicker/>)}
      </FormItem>

      <FormItem style={{marginBottom: 0}}>
        <div>
          <Button
            type="danger"
            htmlType="reset"
            style={{float: 'right', marginRight: '10px'}}
            loading={props.resetting}
          >
            {props.resetting ? 'Resetting' : 'Reset'}
          </Button>
          <Button
            type="primary"
            htmlType="submit"
            style={{float: 'right', marginRight: '10px'}}
            loading={props.submitting}
          >
            {props.submitting ? 'Submitting' : 'Submit'}
          </Button>
        </div>
      </FormItem>
    </Form>
  );
});
export default FlatListFilter;
