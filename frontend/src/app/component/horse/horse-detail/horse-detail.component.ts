import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HorseService } from 'src/app/service/horse.service';
import { Horse } from 'src/app/dto/horse';
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';

@Component({
  selector: 'app-horse-detail',
  standalone: true,
  templateUrl: './horse-detail.component.html',
  styleUrls: ['./horse-detail.component.scss'],
  imports: [CommonModule, RouterLink, ConfirmDeleteDialogComponent],
})
export class HorseDetailComponent implements OnInit {
  horse?: Horse;
  error?: string;
  loading = true;

  constructor(
    private service: HorseService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (!Number.isFinite(id)) {
        this.error = 'Invalid horse id';
        this.loading = false;
        return;
      }
      this.loadHorse(id);
    });
  }

  private loadHorse(id: number): void {
    this.loading = true;
    this.service.getById(id).subscribe({
      next: h => { this.horse = h; this.loading = false; },
      error: err => {
        this.error = err?.status === 404 ? 'Horse not found.' :
          (err?.error?.message ?? 'Could not load horse');
        this.loading = false;
      }
    });
  }

  deleteHorse(): void {
    if (!this.horse?.id) return;
    this.service.delete(this.horse.id).subscribe({
      next: () => this.router.navigate(['/horses']),
      error: err => this.error = err?.error?.message ?? 'Could not delete horse'
    });
  }

  localeDate(): string {
    if (!this.horse?.dateOfBirth) return '';
    const d = this.horse.dateOfBirth instanceof Date
      ? this.horse.dateOfBirth
      : new Date(this.horse.dateOfBirth as unknown as string);
    return isNaN(d.getTime()) ? '' : d.toLocaleDateString();
  }
}
