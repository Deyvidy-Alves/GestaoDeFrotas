# 🚗 Sistema de Gestão de Frotas e Locação

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)

## 📋 Sobre o Projeto
O **Gestão de Frotas** é uma aplicação desktop nativa desenvolvida do zero com **Java puro e JavaFX**. O projeto nasceu como uma iniciativa pessoal com o objetivo principal de consolidar fundamentos de Programação Orientada a Objetos (POO), estruturação de dados e o desenvolvimento de interfaces gráficas.

O sistema simula o controle logístico e operacional de uma empresa de locação de veículos, resolvendo na lógica problemas reais como controle de disponibilidade e acompanhamento de frota.

## 🚀 Funcionalidades Principais
* **Cadastro de Veículos:** Gerenciamento da frota, inserindo dados como placa, modelo e categoria.
* **Controle de Disponibilidade:** Acompanhamento em tempo real de quais veículos estão locados, em manutenção ou disponíveis no pátio.
* **Gestão de Locações e Manutenções:** (Descreva aqui se o sistema registra saídas de veículos para clientes ou idas para a oficina).
* **Interface Intuitiva:** Telas limpas e diretas construídas com arquivos FXML, separando a regra de negócio da visualização.

## 🛠️ Tecnologias Utilizadas
* **Back-end:** Java (Foco total em Lógica e POO)
* **Front-end / UI:** JavaFX (Scene Builder, FXML e Controllers)
* **Banco de Dados:** MySQL
* **Arquitetura:** MVC (Model-View-Controller)

## 💻 Como Executar o Projeto Localmente

### Pré-requisitos
* JDK 17 (ou superior) instalado na máquina.
* IDE (IntelliJ IDEA) configurada para rodar projetos JavaFX.

### Passos
1. Faça o clone do repositório:
   ```bash
   git clone https://github.com/Deyvidy-Alves/GestaoDeFrotas
    ´´´
2. Abra a pasta do projeto na sua IDE favorita.

3. Certifique-se de configurar as dependências do JavaFX no seu module-info.java ou nas bibliotecas do projeto.

4. Configure a string de conexão na sua classe de persistência e rode o script SQL para criar as tabelas.

5. Execute o arquivo principal **HelloApplication** para abrir a tela inicial do sistema.