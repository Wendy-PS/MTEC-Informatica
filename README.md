# 💻 Sistema MTEC Informática

Um sistema desktop robusto de gerenciamento focado no controle de clientes, equipe técnica e acompanhamento de ordens de serviço. 

Este projeto foi desenvolvido integralmente em **Java** e tem como objetivo demonstrar a aplicação prática de padrões de projeto (Design Patterns), criação de interfaces gráficas nativas e persistência de dados embutida.

---

## 🚀 Como testar a aplicação (Executável)
Se você deseja apenas ver o sistema funcionando sem compilar o código, eu preparei um pacote autônomo (não requer instalação de banco de dados).

Acesse a aba **[Releases](../../releases)** no lado direito do repositório, que você encontrará todas as instruções de como prosseguir.

----

## 🏗️ Arquitetura e Estruturação do Código

Para garantir que o código fosse escalável, limpo e de fácil manutenção, o projeto foi estruturado seguindo o padrão **MVC (Model-View-Controller)**:

*   **View (Interfaces):** Telas desenvolvidas em **Java Swing** (`JFrame`, `JPanel`), desenhadas para oferecer uma experiência de usuário intuitiva. As views não contêm regras de negócio.
*   **Controller (Controle):** Camada responsável por capturar os eventos da interface (cliques de botão, preenchimento de campos) e coordenar a comunicação entre a interface e os dados.
*   **Model (Lógica e Dados):** Classes que representam as entidades do sistema (Cliente, Ordem de Serviço, Funcionário).

### 🗄️ Persistência de Dados e Banco H2

Uma das premissas do projeto era facilitar a distribuição. Para isso, o banco de dados utilizado é o **H2 Database** em modo *embedded* (embutido). 

- **Por que o H2?** Ele permite que o sistema rode localmente sem que o usuário final precise instalar e configurar um servidor pesado como MySQL ou PostgreSQL. O banco é gerado e lido a partir de um arquivo local que viaja junto com a aplicação.
- **Padrão DAO:** A comunicação com o H2 foi feita utilizando o padrão **Data Access Object (DAO)**, isolando todas as queries SQL (CRUD - Create, Read, Update, Delete) do restante do sistema.

## 🛠️ Tecnologias Utilizadas

*   **Linguagem:** Java (Orientação a Objetos)
*   **GUI:** Java Swing
*   **Banco de Dados:** H2 Database Engine
*   **Gerenciamento/Build:** (compilação nativa da IDE)

## ✨ Principais Funcionalidades (CRUD)

*   **Gestão de Clientes:** Cadastro, edição, busca e exclusão de perfis de clientes.
*   **Controle de Equipe:** Gerenciamento dos dados do quadro de funcionários/técnicos.
*   **Ordens de Serviço:** Criação de OS, atribuição de técnicos e acompanhamento de status.

---
**Desenvolvido com muito carinho, por [Wendy](https://github.com/Wendy-PS)**
