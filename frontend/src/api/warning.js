import request from './request'

export const getWarningList = (params) => {
    return request({
        url: '/warning/list',
        method: 'get',
        params
    })
}
