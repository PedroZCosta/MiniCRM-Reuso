using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers
{
    public class MotivoPerdasController : Controller
    {
        private readonly MiniCrmContext _context;

        public MotivoPerdasController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: MotivoPerdas
        public async Task<IActionResult> Index()
        {
            return View(await _context.MotivoPerdas.ToListAsync());
        }

        // GET: MotivoPerdas/Details/5
        public async Task<IActionResult> Details(sbyte? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var motivoPerda = await _context.MotivoPerdas
                .FirstOrDefaultAsync(m => m.IdMotivo == id);
            if (motivoPerda == null)
            {
                return NotFound();
            }

            return View(motivoPerda);
        }

        // GET: MotivoPerdas/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: MotivoPerdas/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("IdMotivo,Descricao")] MotivoPerda motivoPerda)
        {
            if (ModelState.IsValid)
            {
                _context.Add(motivoPerda);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            return View(motivoPerda);
        }

        // GET: MotivoPerdas/Edit/5
        public async Task<IActionResult> Edit(sbyte? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var motivoPerda = await _context.MotivoPerdas.FindAsync(id);
            if (motivoPerda == null)
            {
                return NotFound();
            }
            return View(motivoPerda);
        }

        // POST: MotivoPerdas/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(sbyte id, [Bind("IdMotivo,Descricao")] MotivoPerda motivoPerda)
        {
            if (id != motivoPerda.IdMotivo)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(motivoPerda);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!MotivoPerdaExists(motivoPerda.IdMotivo))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            return View(motivoPerda);
        }

        // GET: MotivoPerdas/Delete/5
        public async Task<IActionResult> Delete(sbyte? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var motivoPerda = await _context.MotivoPerdas
                .FirstOrDefaultAsync(m => m.IdMotivo == id);
            if (motivoPerda == null)
            {
                return NotFound();
            }

            return View(motivoPerda);
        }

        // POST: MotivoPerdas/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(sbyte id)
        {
            var motivoPerda = await _context.MotivoPerdas.FindAsync(id);
            if (motivoPerda != null)
            {
                _context.MotivoPerdas.Remove(motivoPerda);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool MotivoPerdaExists(sbyte id)
        {
            return _context.MotivoPerdas.Any(e => e.IdMotivo == id);
        }
    }
}
