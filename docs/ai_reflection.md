# AI Integration Reflection

This reflection critiques the use, effectiveness, and limitations of AI assistance in the design and development of the SDG-1 Microfinance Tracker.

## What AI Was Most Useful For
AI assistance was exceptionally valuable for scaffolding the visual components and boilerplate layout structures of the ScalaFX user interface. Writing nested grid panes, defining padding, and setting consistent button styles in ScalaFX requires highly repetitive and verbose code. As logged in entry #1 and entry #2, using AI to generate these GUI containers dramatically accelerated the early development phases. Furthermore, AI was highly effective in generating initial templates for parsing tabular file formats like CSV, saving time that would have otherwise been spent writing standard regex splits and string transformations.

## Where AI Misled Me and Hallucinated
Despite its speed, AI frequently struggled with the strict type constraints of Scala 3 and ScalaFX's Java-wrapped properties. A clear instance of this occurred in entry #4, where the AI suggested using `headerText = null` inside a ScalaFX `Alert` dialog block. This suggestion caused a critical compile-time error because `headerText_=` is overloaded in ScalaFX, accepting both `String` and `Option[String]` parameters. Passing `null` created method signature ambiguity that the compiler could not resolve. To fix this, I had to replace the null assignments with Scala-native `None` and `Some` options. Additionally, in entry #9, the AI proposed using JavaFX `setOnAction` event handlers instead of the ScalaFX `onAction = handle` idiom, which would have violated the project's GUI coding standards.

## What I Did That AI Could Not Do
While the AI was adept at providing separate code blocks, it was incapable of reasoning about the cohesive system design and validation constraints. I had to manually implement the business logic that enforces data integrity, such as verifying that a user-entered repayment amount does not exceed the outstanding balance of the selected loan. The AI also could not check for compilation warning constraints like `-Wunused`, which required me to clean up redundant imports and structure the build configuration. Lastly, the AI could not perform the live manual testing to verify that saved CSV files correctly reloaded state across application restarts, ensuring persistence.
