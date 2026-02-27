import request from './request'

export const getExamScoreList = (params) => {
  return request.get('/exam/list', { params })
}

export const addExamScore = (data) => {
  return request.post('/exam/add', data)
}

export const updateExamScore = (data) => {
  return request.put('/exam/update', data)
}

export const deleteExamScore = (id) => {
  return request.delete(`/exam/delete/${id}`)
}

export const getExamStats = (params) => {
  return request.get('/exam/stats', { params })
}

export const getStudentScores = (studentId) => {
  return request.get(`/exam/student/${studentId}`)
}

