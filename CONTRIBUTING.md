# Contributing to LobbyLynx


First off, thank you for considering contributing to LobbyLynx! It's people like you that make LobbyLynx such a great tool.


## Code of Conduct


By participating in this project, you are expected to uphold our [Code of Conduct](CODE_OF_CONDUCT.md).


## How Can I Contribute?


### Reporting Bugs


Before creating bug reports, please check the [issue list](https://github.com/DerJannik/LobbyLynx/issues) as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:


* **Use a clear and descriptive title**

* **Describe the exact steps to reproduce the bug**

* **Provide specific examples to demonstrate the steps**

* **Describe the behavior you observed after following the steps**

* **Explain which behavior you expected to see instead and why**

* **Include screenshots if possible**

* **Include your server version and plugin version**

* **Include any relevant logs**


### Suggesting Enhancements


Enhancement suggestions are tracked as [GitHub issues](https://github.com/DerJannik/LobbyLynx/issues). When creating an enhancement suggestion, please include:


* **Use a clear and descriptive title**

* **Provide a step-by-step description of the suggested enhancement**

* **Provide specific examples to demonstrate the steps**

* **Describe the current behavior and explain the behavior you expected to see instead**

* **Explain why this enhancement would be useful**

* **List some other plugins or applications where this enhancement exists**


### Pull Requests


#### Setting Up Your Development Environment


1. Fork the repository

2. Clone your fork: `git clone https://github.com/your-username/LobbyLynx.git`

3. Create a new branch: `git checkout -b feature/your-feature-name`

4. Set up your development environment:

   - Install JDK 17 or higher

   - Use an IDE (preferably IntelliJ IDEA)

   - Install Maven


#### Development Workflow


1. Create your feature branch (`git checkout -b feature/amazing-feature`)

2. Make your changes

3. Update the documentation if needed

4. Add tests if applicable

5. Commit your changes (`git commit -m 'Add some amazing feature'`)

6. Push to the branch (`git push origin feature/amazing-feature`)

7. Open a Pull Request


#### Coding Standards


- Follow the existing code style

- Use meaningful variable and method names

- Comment your code where necessary

- Keep methods focused and concise

- Write clean, readable code

- Follow Java naming conventions


#### Java Code Style Guidelines


```java

public class ExampleClass {

    private final String example;

    

    public ExampleClass(String example) {

        this.example = example;

    }

    

    /**

     * Method description

     * @param parameter Description of parameter

     * @return Description of return value

     */

    public String doSomething(String parameter) {

        // Code here

    }

}

Testing

    Write unit tests for new features
    Ensure all tests pass before submitting PR
    Test your changes in a real server environment
    Document any new test cases

Documentation

    Update README.md if needed
    Add comments to your code
    Update wiki pages if applicable
    Create/update API documentation

Project Structure

LobbyLynx/

├── src/

│   ├── main/

│   │   ├── java/

│   │   │   └── org/derjannik/lobbylynx/

│   │   └── resources/

│   └── test/

├── docs/

├── .gitignore

├── pom.xml

├── README.md

└── LICENSE

Build Process

    Ensure you have Maven installed
    Run mvn clean install
    Find the built jar in target/

Review Process

    A maintainer will review your PR
    Changes may be requested
    Once approved, your PR will be merged
    Your contribution will be added to the changelog

Additional Notes
Issue and Pull Request Labels

    bug: Something isn't working
    enhancement: New feature or request
    documentation: Improvements or additions to documentation
    help-wanted: Extra attention is needed
    good-first-issue: Good for newcomers

Communication

    Join our Discord server for real-time discussion
    Use GitHub issues for bug reports and feature requests
    Use pull requests for code contributions

Recognition

Contributors will be added to the Contributors list in the README.md file.
Questions?

If you have any questions, create an issue, reach out on Discord, or send us an email at [contact@email.com].

Thank you for contributing to LobbyLynx!


This Markdown file is now properly formatted and ready to be saved as `CONTRIBUTING.md`. Remember to replace placeholder values like `[discord-link]` and `[contact@email.com]` with actual values for your project.
