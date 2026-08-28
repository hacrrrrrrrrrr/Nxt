with open("gradle/libs.versions.toml", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith("firebase-messaging"):
        continue
    if line.strip() == "[plugins]":
        new_lines.append("firebase-messaging = { group = \"com.google.firebase\", name = \"firebase-messaging\" }\n")
    new_lines.append(line)

with open("gradle/libs.versions.toml", "w") as f:
    f.writelines(new_lines)
