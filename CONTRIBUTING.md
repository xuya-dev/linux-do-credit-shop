# Contributing to LDC Shop / 贡献指南

Thank you for your interest in contributing to LDC Shop! This guide will help you get started with the development process.

感谢您对 LDC Shop 的关注！本指南将帮助您了解开发流程。

---

## Development Setup / 开发环境准备

### Prerequisites / 前置要求

| Tool | Version | Notes |
|------|---------|-------|
| Java | 17 | Required for Spring Boot 3.2 |
| Node.js | >= 20.19.0 | Frontend build |
| pnpm | >= 10.0.0 | Package manager (required) |
| MySQL | 8.x | Database |
| Redis | 7.x | Cache & session |
| Git | Latest | Version control |

### Clone the Repository / 克隆仓库

```bash
git clone https://github.com/xuya-dev/linux-do-credit-shop.git
cd linux-do-credit-shop
```

### Backend Setup / 后端配置

1. Create a MySQL database and import the schema:

```bash
mysql -u root -p < sql/init.sql
```

2. Configure the backend application (edit `backend/src/main/resources/application.yml` or `application-dev.yml`) with your local MySQL and Redis connection details.

3. Build and run the backend:

```bash
cd backend
./mvnw spring-boot:run
```

The backend API will be available at `http://localhost:8080`.

### Frontend Setup / 前端配置

1. Install dependencies:

```bash
cd frontend
pnpm install
```

2. Start the development server:

```bash
pnpm dev
```

The frontend dev server will be available at `http://localhost:5174` (default Vite port).

3. Build for production:

```bash
pnpm build
```

### Docker Setup / Docker 开发

You can also use Docker for a quick local environment:

```bash
cp .env.example .env
# Edit .env with your configuration
./deploy.sh standalone
```

---

## Code Style / 代码风格

### Java (Backend)

- Follow standard Java naming conventions (camelCase for variables/methods, PascalCase for classes)
- Use Lombok annotations where appropriate (`@Data`, `@Builder`, `@Slf4j`, etc.)
- Keep controllers thin; put business logic in service classes
- Use MyBatis-Plus conventions for mapper interfaces
- Add meaningful Javadoc for public APIs

### TypeScript / Vue 3 (Frontend)

- Follow the project's ESLint configuration — run `pnpm lint` to check
- Use Composition API with `<script setup>` syntax
- Follow the existing file organization patterns under `src/`
- Use TypeScript strict mode; avoid `any` types
- Use Naive UI components consistently

### General / 通用

- Use **UTF-8** encoding for all files
- Use **LF** line endings (configured in `.editorconfig`)
- Use **2-space** indentation for YAML, JSON, and SQL files
- Use **4-space** indentation for Java and TypeScript files
- Always add a final newline at the end of files
- Trim trailing whitespace (except in Markdown files)

---

## Commit Messages / 提交信息

This project follows [Conventional Commits](https://www.conventionalcommits.org/) format:

本项目遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types / 类型

| Type | Description |
|------|-------------|
| `feat` | New feature / 新功能 |
| `fix` | Bug fix / 修复缺陷 |
| `docs` | Documentation only / 仅文档 |
| `style` | Code style (formatting, etc.) / 代码风格 |
| `refactor` | Code refactoring / 代码重构 |
| `perf` | Performance improvement / 性能优化 |
| `test` | Adding or updating tests / 测试相关 |
| `chore` | Build process or tooling / 构建/工具 |
| `ci` | CI/CD changes / 持续集成 |

### Examples / 示例

```
feat(backend): add order export API
fix(frontend): resolve login redirect loop
docs: update deployment guide
chore(deps): upgrade Spring Boot to 3.2.5
```

---

## Pull Request Process / 提交 PR 流程

### Before Submitting / 提交前

1. **Fork** the repository and create your branch from `main`.
2. **Test** your changes thoroughly.
3. **Lint** your code:
   - Frontend: `pnpm lint`
   - Backend: Ensure Maven build passes (`./mvnw verify`)
4. **Commit** with descriptive messages following Conventional Commits.

### Creating a PR / 创建 PR

1. Fill out the PR template completely.
2. Reference any related issues (e.g., `Fixes #123`).
3. Keep PRs focused — one feature or fix per PR when possible.
4. Ensure all CI checks pass.

### Review Process / 审核流程

1. A maintainer will review your code.
2. Address any review feedback by pushing new commits.
3. Once approved, a maintainer will merge your PR.

### PR Checklist / PR 检查清单

- [ ] Code compiles without errors
- [ ] Changes tested locally
- [ ] No unnecessary files committed (e.g., build artifacts, `.env`)
- [ ] Commit messages follow Conventional Commits
- [ ] Documentation updated if needed

---

## Reporting Issues / 报告问题

- Use GitHub Issues for bug reports and feature requests.
- Fill out the issue template completely.
- For **security vulnerabilities**, please refer to [SECURITY.md](./SECURITY.md).

---

## License / 许可证

By contributing to LDC Shop, you agree that your contributions will be licensed under the [MIT License](./LICENSE).

贡献代码即表示您同意按照 [MIT License](./LICENSE) 许可您的贡献。
