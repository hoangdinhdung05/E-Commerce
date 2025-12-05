import { Component, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';

/**
 * Base component with automatic subscription cleanup
 * Prevents memory leaks by providing destroy$ subject
 * 
 * @example
 * ```typescript
 * export class MyComponent extends DestroyableComponent implements OnInit {
 *   ngOnInit() {
 *     this.myService.getData()
 *       .pipe(takeUntil(this.destroy$))
 *       .subscribe(data => {...});
 *   }
 *   // No need to implement ngOnDestroy, parent handles it
 * }
 * ```
 */
@Component({
  template: ''
})
export abstract class DestroyableComponent implements OnDestroy {
  /**
   * Subject that emits when component is destroyed
   * Use with takeUntil() operator to auto-unsubscribe observables
   */
  protected destroy$ = new Subject<void>();

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
