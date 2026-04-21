# Security Policy / 安全政策

## Reporting a Vulnerability / 报告漏洞

**English:**

We take the security of LDC Shop seriously. If you believe you have found a security vulnerability, please report it to us so we can address it promptly.

**Please do NOT report security vulnerabilities through public GitHub issues.**

Instead, please report vulnerabilities through one of the following channels:

- **GitHub Issues**: Use the [Security Advisory](https://github.com/xuya-dev/linux-do-credit-shop/security/advisories/new) feature to privately report a vulnerability.
- **Email**: Send details to [dev@xuya.dev](mailto:dev@xuya.dev). Please include:
  - A description of the vulnerability
  - Steps to reproduce the issue
  - Potential impact
  - Any suggested fixes (optional)

### What to Expect

1. We will acknowledge receipt of your report within **48 hours**.
2. We will provide an estimated timeline for a fix within **5 business days**.
3. We will notify you when the vulnerability has been resolved.
4. We will credit you in the fix release notes (unless you prefer to remain anonymous).

### Scope

The following are considered in scope:
- Authentication and authorization bypasses
- SQL injection, XSS, CSRF, or other injection vulnerabilities
- Sensitive data exposure (credentials, tokens, user data)
- Privilege escalation flaws
- Payment-related security issues

The following are **out of scope**:
- Denial of Service (DoS) attacks
- Social engineering
- Physical attacks
- Vulnerabilities in third-party dependencies (please report to the respective maintainers)
- Issues in non-default configurations

---

**中文:**

我们非常重视 LDC Shop 的安全性。如果您发现安全漏洞，请及时向我们报告。

**请不要通过公开的 GitHub Issues 报告安全漏洞。**

请通过以下渠道报告漏洞：

- **GitHub Issues**: 使用 [Security Advisory](https://github.com/xuya-dev/linux-do-credit-shop/security/advisories/new) 功能私下报告漏洞。
- **电子邮件**: 发送详情至 [dev@xuya.dev](mailto:dev@xuya.dev)。请包含以下内容：
  - 漏洞描述
  - 复现步骤
  - 潜在影响
  - 建议修复方案（可选）

### 响应流程

1. 我们将在 **48 小时内**确认收到您的报告。
2. 我们将在 **5 个工作日内**提供修复时间表。
3. 漏洞修复后我们会通知您。
4. 我们会在修复版本的发布说明中致谢（除非您希望匿名）。
