/**
 * 通用工具函数
 */

/** 格式化金额（保留2位小数） */
export function formatAmount(amount: number | undefined): string {
  if (amount === undefined || amount === null) return '0.00';
  return amount.toFixed(2);
}

/** 格式化日期（简化版） */
export function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

/** 格式化日期（仅日期） */
export function formatDateOnly(dateStr: string | undefined): string {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

/** 订单号脱敏 */
export function maskOrderNo(orderNo: string): string {
  if (!orderNo || orderNo.length <= 8) return orderNo;
  return `${orderNo.slice(0, 4)}****${orderNo.slice(-4)}`;
}

/** 复制到剪贴板 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    return false;
  }
}
