#!/usr/bin/env python3
"""Prints everything a language has taught so far, as JSON.

Hand the output to the model that writes the next level so it only reviews
items the learner has already met.

    tools/content_index.py italiano            # every level
    tools/content_index.py italiano --until 3  # levels 0000 to 0003
"""
import argparse
import json
from pathlib import Path

CONTENT = Path(__file__).resolve().parent.parent / "app/src/main/assets/content"


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("language", help="folder name under content/")
    parser.add_argument("--until", type=int, help="last level number to include")
    args = parser.parse_args()

    taught, levels = {}, []
    for path in sorted((CONTENT / args.language).glob("level-*/level.json")):
        number = int(path.parent.name.split("-")[1])
        if args.until is not None and number > args.until:
            break
        level = json.loads(path.read_text(encoding="utf-8"))
        new = [item for exercise in level["exercises"] for item in exercise.get("new", [])]
        for item in new:
            taught.setdefault(item, {"meaning": level["glossary"].get(item), "level": path.parent.name})
        levels.append({"id": path.parent.name, "type": level["type"], "title": level["title"], "new": new})

    next_id = f"level-{len(levels):04d}"
    print(json.dumps({"language": args.language, "next": next_id, "levels": levels, "taught": taught},
                     ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
