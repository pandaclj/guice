package com.google.inject.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binding;
import com.google.inject.spi.ErrorDetail;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.stream.Collectors;

/** Error reported by Guice when a key is bound at multiple places the injector. */
final class BindingAlreadySetError extends ErrorDetail<BindingAlreadySetError> {
  private final Binding<?> binding;
  private final Binding<?> original;

  BindingAlreadySetError(Binding<?> binding, Binding<?> original, List<Object> sources) {
    super(
        String.format("%s was bound multiple times.", Messages.convert(binding.getKey())),
        sources,
        null);
    this.binding = binding;
    this.original = original;
  }

  @Override
  public boolean isMergeable(ErrorDetail<?> otherError) {
    return otherError instanceof BindingAlreadySetError
        && ((BindingAlreadySetError) otherError).binding.getKey().equals(binding.getKey());
  }

  @Override
  public void format(int index, List<ErrorDetail<?>> mergeableErrors, Formatter formatter) {
    List<List<Object>> sourcesList = new ArrayList<>();
    sourcesList.add(ImmutableList.of(original.getSource()));
    sourcesList.add(ImmutableList.of(binding.getSource()));
    sourcesList.addAll(
        mergeableErrors.stream()
            .map(e -> ((BindingAlreadySetError) e).binding.getSource())
            .map(ImmutableList::of)
            .collect(Collectors.toList()));

    formatter.format("%s) %s %s%n%n", index, "[Guice/BindingAlreadySet]", getMessage());
    formatter.format("Bound at the following %s locations:%n", sourcesList.size());
    for (int i = 0; i < sourcesList.size(); i++) {
      ErrorFormatter.formatSources(i + 1, sourcesList.get(i), formatter);
    }
    formatter.format("%n");
  }

  @Override
  public BindingAlreadySetError withSources(List<Object> newSources) {
    return new BindingAlreadySetError(binding, original, newSources);
  }
}
