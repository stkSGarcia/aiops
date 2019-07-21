import { createStore } from 'redux';

const storeReducer = (state, action) => {
  switch (action.type) {
    case 'inceptor-date':
      return {...state, date: action.data};
    case 'inceptor-view':
      return {...state, view: action.data};
    case 'inceptor-timeline':
      const timeline = state.timeline || {};
      timeline[action.date] = action.data;
      state.timeline = timeline;
      return state;
    case 'inceptor-list':
      const list = state.list || {};
      list[action.date] = action.data;
      state.list = list;
      return state;
    case 'reset':
      return undefined;
    default:
      return state;
  }
};

export const Store = createStore(storeReducer);
export default Store;
