// 简易事件总线，避免引入额外依赖
const bus = {
  _events: {},
  on (type, handler) {
    (this._events[type] || (this._events[type] = [])).push(handler)
  },
  off (type, handler) {
    const list = this._events[type]
    if (list) {
      this._events[type] = list.filter(fn => fn !== handler)
    }
  },
  emit (type, payload) {
    const list = this._events[type] || []
    list.forEach(fn => fn(payload))
  }
}

export default bus
