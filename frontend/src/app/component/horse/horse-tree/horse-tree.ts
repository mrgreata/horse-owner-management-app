import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HorseService } from 'src/app/service/horse.service';
import { Horse } from 'src/app/dto/horse';

@Component({
  standalone: true,
  imports: [CommonModule],
  selector: 'app-horse-tree',
  template: `
    <div class="container mt-3">
      <h3>Pedigree for {{ rootHorse?.name }}</h3>

      <ng-container *ngIf="rootHorse; else loading">
        <!-- simplest possible layout: child, then parents -->
        <div class="card p-2 mb-3">
          <b>{{ rootHorse?.name }}</b> ({{ rootHorse?.sex }}) – {{ rootHorse?.dateOfBirth | date:'yyyy-MM-dd' }}
        </div>

        <div class="row">
          <div class="col">
            <h5>Mother</h5>
            <div *ngIf="mother; else none"> {{ mother.name }} </div>
            <ng-template #none>—</ng-template>
          </div>
          <div class="col">
            <h5>Father</h5>
            <div *ngIf="father; else none2"> {{ father.name }} </div>
            <ng-template #none2>—</ng-template>
          </div>
        </div>
      </ng-container>

      <ng-template #loading>Loading…</ng-template>
    </div>
  `,
})
export class HorseTreeComponent implements OnInit {
  id!: number;
  rootHorse: Horse | null = null;
  mother: Horse | null = null;
  father: Horse | null = null;

  constructor(
    private route: ActivatedRoute,
    private horses: HorseService,
  ) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.horses.getById(this.id).subscribe(h => {
      this.rootHorse = h;
      const mId = (h as any).motherId as number | null | undefined;
      const fId = (h as any).fatherId as number | null | undefined;
      if (mId) this.horses.getById(mId).subscribe(x => this.mother = x);
      if (fId) this.horses.getById(fId).subscribe(x => this.father = x);
    });
  }
}
