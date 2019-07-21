import { CSSProperties } from 'react';

export const pieCharOption = (data: any) => {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    series: [{
      type: 'pie',
      name: 'Component',
      data: data,
      radius: ['37%', '57%'],
      label: {
        fontSize: 16,
        formatter: ' {b|{b}: }{c} {per|{d}%} ',
        backgroundColor: '#eeeeee',
        borderColor: '#aaaaaa',
        borderWidth: 1,
        borderRadius: 4,
        rich: {
          b: {
            fontSize: 16,
            lineHeight: 33
          },
          per: {
            color: '#eeeeee',
            backgroundColor: '#334455',
            padding: [2, 4],
            borderRadius: 2
          }
        },
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  };
};
export const pieNumber = (num: number): CSSProperties => {
  return {
    fontWeight: 'bold',
    fontSize: num > 9999 ? '34px' : '40px',
    color: 'rgba(58, 60, 77, 0.5)',
    position: 'absolute',
    display: 'flex',
    top: 0, left: 0, bottom: 0, right: 0,
    justifyContent: 'center',
    alignItems: 'center',
  };
};
export const lineChartOption = (legend: any, data: any) => {
  return {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c} times',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985',
        },
      },
    },
    xAxis: {
      type: 'category',
      data: legend,
    },
    yAxis: {
      type: 'value',
    },
    series: [{
      data: data,
      type: 'line',
      label: {
        normal: {
          show: true,
          position: 'top',
        },
      },
    }],
  };
};
