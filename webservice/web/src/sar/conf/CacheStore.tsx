import { createStore } from 'redux';

const storeReducer = (state, action) => {
  switch (action.type) {
    case 'sarReportBean':
      return {
        ...state,
        sarReports: action.sarReports,
      };
    case 'reset':
      return undefined;
    default:
      return state;
  }
};

export const Store = createStore(storeReducer);
export default Store;
