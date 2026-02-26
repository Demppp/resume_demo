import request from './request'

export const getStudentList = (params) => {
  return request.get('/student/list', { params })
}

export const addStudent = (data) => {
  return request.post('/student/add', data)
}

export const addStudentByAi = (data) => {
  return request.post('/student/add-by-ai', data)
}

export const updateStudent = (data) => {
  return request.put('/student/update', data)
}

export const deleteStudent = (id) => {
  return request.delete(`/student/delete/${id}`)
}

export const getStudentById = (id) => {
  return request.get(`/student/detail/${id}`)
}

export const getAllClasses = () => {
  return request.get('/student/classes')
}

