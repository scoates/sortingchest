# Sorting Chest
## Super short how-to:

- give users the sortingchest.sort permission
- have a user create and place a chest named "source" (by default; see source_chest_name below)
- within sort_distance blocks, have the user place one or more chests named "destination" (see destination_chest_name below)
- in those destination chests, the user should add items (incomplete stacks)
- then, when the user places items into the "source" chest, SorterChest will automatically sort the items into available stack space in the "destination" chest
