import request from './request'

export const getDiaryList = (params) => {
  return request.get('/diary/list', { params })
}

export const addDiary = (data) => {
  return request.post('/diary/add', data)
}

export const updateDiary = (data) => {
  return request.put('/diary/update', data)
}

export const deleteDiary = (id) => {
  return request.delete(`/diary/delete/${id}`)
}

export const getDiaryById = (id) => {
  return request.get(`/diary/detail/${id}`)
}

