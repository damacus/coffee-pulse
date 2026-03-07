## 2024-03-07 - Add htmlFor and id to labels and inputs
**Learning:** React labels and inputs should be connected using `htmlFor` on the `<label>` and a corresponding `id` on the `<input>` element. Without this, click targets for the labels will not focus the input fields, and screen reader announcements will not be properly associated with the form inputs.
**Action:** When working on generic configuration forms without standard component libraries, ensure any `<label>` and `<input>` pair explicitly map their identifiers for accessibility.
