// 统一占位图：石墨黑背景 + 香槟金商品图标，SVG内联加载秒开
const PLACEHOLDER = `data:image/svg+xml,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="60%" y2="100%">
      <stop offset="0%" stop-color="#2d2d31"/>
      <stop offset="100%" stop-color="#1a1a1e"/>
    </linearGradient>
    <linearGradient id="gold" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#f0d98a"/>
      <stop offset="50%" stop-color="#d4af37"/>
      <stop offset="100%" stop-color="#b8941f"/>
    </linearGradient>
    <linearGradient id="goldSoft" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#e8c674"/>
      <stop offset="100%" stop-color="#c9a96e"/>
    </linearGradient>
    <radialGradient id="glow" cx="50%" cy="45%" r="50%">
      <stop offset="0%" stop-color="#d4af37" stop-opacity="0.12"/>
      <stop offset="100%" stop-color="#d4af37" stop-opacity="0"/>
    </radialGradient>
    <filter id="shadow">
      <feGaussianBlur stdDeviation="3"/>
    </filter>
  </defs>

  <rect width="400" height="400" rx="0" fill="url(#bg)"/>
  <rect width="400" height="400" fill="url(#glow)"/>

  <g transform="translate(200,175)">
    <ellipse cx="0" cy="48" rx="40" ry="8" fill="#000" opacity="0.3" filter="url(#shadow)"/>

    <path d="M-34,-12 L34,-12 L39,45 L-39,45 Z"
          fill="url(#goldSoft)" stroke="url(#gold)" stroke-width="1.5" stroke-linejoin="round"/>

    <path d="M-18,-12 Q-18,-33 0,-33 Q18,-33 18,-12"
          fill="none" stroke="url(#gold)" stroke-width="2.5" stroke-linecap="round"/>

    <rect x="-12" y="5" width="24" height="3" rx="1.5" fill="#1a1a1e" opacity="0.25"/>
    <rect x="-8" y="16" width="16" height="3" rx="1.5" fill="#1a1a1e" opacity="0.25"/>

    <circle cx="0" cy="2" r="2.5" fill="#1a1a1e" opacity="0.2"/>
  </g>

  <text x="200" y="262" text-anchor="middle"
        font-family="'PingFang SC','Microsoft YaHei',sans-serif"
        font-size="13" fill="#8a7a4a" letter-spacing="3">
    商品图片
  </text>
</svg>
`)}`

export function getProductImage(product) {
  if (product.image && product.image.startsWith('http')) {
    return product.image
  }
  return PLACEHOLDER
}

export function getProductImageLandscape(product) {
  if (product.image && product.image.startsWith('http')) {
    return product.image
  }
  return PLACEHOLDER
}
